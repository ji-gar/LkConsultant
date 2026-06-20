# Reverb Realtime Chat — Changes Log

Setup of Laravel Reverb realtime chat in the LkConsultant Android app, mirrored from the reference projects:
- `D:/anvitsystem github/lkedc-portal` (Laravel backend with Reverb + `MessageSent` event)
- `D:/anvitsystem github/lkedc-frontend` (Next.js frontend using `laravel-echo` + `pusher-js`)
- `D:/anvitsystem github/lkedc-portal/realtime-chat-kotlin` (reference Kotlin module)

## Files changed

### 1. `app/src/main/java/com/io/lkconsultants/reverb/ReverbManager.kt`
Full rewrite. Key points:
- Separated `connect()` from `subscribeConversation()` so the same socket can host multiple channels.
- Tracks subscribed channels in a `ConcurrentHashMap` for clean teardown via `unsubscribe(id)` / `disconnect()`.
- Exposes connection `State` (IDLE / CONNECTING / CONNECTED / DISCONNECTED / FAILED) plus listener registration.
- Auth headers send `Authorization: Bearer <TokenProvider.getToken()>` and `Accept: application/json`.
- **Bug fix**: auth URL changed from `https://lkedc.free.laravel.cloud/broadcasting/auth` to `https://lkedc.free.laravel.cloud/api/broadcasting/auth` to match what `lkedc-frontend/src/lib/config.ts` (`REVERB_AUTH_ENDPOINT`) uses. The wrong URL was the reason live updates never arrived — auth silently failed, so the `private-conversation.X` subscription never activated.
- Added `ChatChannelListener` abstract class that dispatches `MessageSent` / `MessagesRead` / `UserTyping` callbacks with decoded JSON.

### 2. `app/src/main/java/com/io/lkconsultants/viewmodel/ChatViewModel.kt`
Full rewrite. Key points:
- Previous version called `ReverbManager.connect(id) {}` with an empty lambda — events arrived but never reached the UI.
- Now adds a state listener, calls `ReverbManager.connect { subscribeConversation(id, channelListener) }`.
- `channelListener.onMessageSent(json)` parses the Laravel payload (handles `{"message": {...}}` wrapper from `App\Events\MessageSent::__construct`) into `MessageResponse` via Gson.
- `appendRealtime(msg)` merges into `MessagesState.Success` with id-based dedup so the post-send `getMessages` refresh and the realtime echo can't both add the same message.
- `onCleared()` removes state listener and unsubscribes the channel.
- Exposes `connection: StateFlow<ReverbManager.State>` for UI status indicators.

### 3. `app/src/main/java/com/io/lkconsultants/view/ChatScreen.kt`
- Imported `androidx.compose.runtime.DisposableEffect`.
- Added `DisposableEffect(id) { onDispose { ReverbManager.unsubscribe(id.toLong()) } }` so leaving the chat screen tears down the channel and channels don't leak across conversations.
- Rewrote `formatTime(isoDate)` to fix broken bubble timestamps:
  - Old version used a single rigid pattern `yyyy-MM-dd'T'HH:mm:ss'Z'` and fell through to printing the raw ISO string when the input was Carbon's default `2026-04-28T12:34:56.000000Z` (microseconds) or `+00:00` offset.
  - New version normalizes the input (trims microseconds to ms, converts `Z` / `+00:00` to `+0000`), tries multiple patterns, and renders in the device's local timezone as: today → `h:mm a`, yesterday → `Yesterday h:mm a`, older → `MMM d, h:mm a`.

## How realtime now flows

1. `ChatScreen` opens → `LaunchedEffect(id)` calls `ChatViewModel.connect(token, id)`.
2. `ReverbManager.connect()` opens a Pusher socket to Reverb at `ws-a192fe47-...laravel.cloud:443`.
3. On `CONNECTED`, `subscribeConversation(id, listener)` sends a subscribe frame for `private-conversation.<id>` with an auth payload obtained from `POST /api/broadcasting/auth`.
4. Backend `routes/channels.php` authorizes via `Broadcast::channel('conversation.{conversationId}', ...)` (Laravel strips the `private-` prefix automatically).
5. When any participant sends a message, `App\Events\MessageSent` fires with `broadcastAs: 'MessageSent'`, payload `{ message: { id, conversation_id, text, file_name, file_url, created_at, sender:{id,name,email,role} } }`.
6. `ChatChannelListener.onMessageSent(json)` → `ChatViewModel.appendRealtime` → `_state` updates → `ChatScreen` recomposes → new bubble appears instantly.

## Backend / wire config (for reference, no changes needed)

| Setting | Value |
| --- | --- |
| Reverb key | `ENmFzvymq1fPqNPieGBV` |
| Reverb host | `ws-a192fe47-b57b-48ff-bfd7-4f574d381592-reverb.laravel.cloud` |
| Reverb port | `443` (TLS) |
| Auth endpoint | `https://lkedc.free.laravel.cloud/api/broadcasting/auth` |
| Channel | `private-conversation.{conversationId}` |
| Event | `MessageSent` |

## Search + New Chat (additional changes)

### New files
- `app/src/main/java/com/io/lkconsultants/model/UsersListResponse.kt` — DTOs: `UsersListResponse`, `ChatUser`, `CreateConversationRequest`, `CreatedConversation`.
- `app/src/main/java/com/io/lkconsultants/viewmodel/NewChatViewModel.kt` — loads `GET /api/users` (filtered to exclude self) and creates a 1-1 conversation via `POST /api/chat/conversations { participantIds:[me, target] }`. Backend returns the existing conversation if one already exists for that pair, otherwise creates a new one — both cases land in `CreateConversationState.Success`.
- `app/src/main/java/com/io/lkconsultants/view/NewChatScreen.kt` — search bar (filters by name/email) + tappable user list. On tap, calls `startConversationWith` and on success, navigates to `ChatScreen` via the `onConversationReady` callback.

### Modified files
- `app/src/main/java/com/io/lkconsultants/retrofit/RetrofitInstance.kt` — added `getUsers(): Response<UsersListResponse>` and `createConversation(request): Response<CreatedConversation>`.
- `app/src/main/java/com/io/lkconsultants/navscreen/Screens.kt` — added `Screens.NewChatScreen` route.
- `app/src/main/java/com/io/lkconsultants/MainActivity.kt` — wired `NewChatScreen` into `NavStack`. After `onConversationReady`, the picker is popped and `ChatScreen(convoId, participantId, name)` is pushed.
- `app/src/main/java/com/io/lkconsultants/view/ListOfChatScreen.kt` — `UsersScreen` now wraps content in a `Scaffold` with:
  - **Search button** (top app bar, right side) — toggles a search field that filters the chat list in-memory by group name, last message, or participant name.
  - **New chat button** — a "+" icon in the app bar plus a primary-blue **FAB** in the bottom-right corner. Both invoke the new `onNewChat` callback that navigates to `NewChatScreen`.

### Flow
1. Chat list → tap "+" (or FAB) → `NewChatScreen`.
2. Type to filter users → tap a row.
3. `POST /api/chat/conversations` is called; on success the screen pops and `ChatScreen` opens for that conversation, with realtime Reverb subscription kicking in via `LaunchedEffect(id)`.

## Unread badges + system notifications

### New file
- `app/src/main/java/com/io/lkconsultants/reverb/ChatNotifier.kt` — notification channel + `activeConversationId` tracker. `showNewMessage(...)` posts a heads-up notification on the `chat_messages` channel, suppressed when the user is currently viewing that conversation. Notification small icon points at `R.mipmap.ic_launcher`; tap deep-links back into `MainActivity` with `conversationId` extra.

### Modified files
- `app/src/main/java/com/io/lkconsultants/model/ConversationResponse.kt` — added `unread_count: Int = 0` (backend `ConversationController@index` already returns this per conversation).
- `app/src/main/java/com/io/lkconsultants/ApplicationClass.kt` — calls `ChatNotifier.ensureChannel(...)` at startup so the OS channel exists before the first notification fires.
- `app/src/main/java/com/io/lkconsultants/viewmodel/UsersViewModel.kt` — now an `AndroidViewModel`. After loading conversations it calls `ReverbManager.connect { subscribeConversation(id, listener) }` for **every** conversation. On `MessageSent`:
  - bumps `unread_count` (unless sender is me or the chat is currently open),
  - rewrites `last_message` / `updated_at` and resorts the list newest-first,
  - posts a `ChatNotifier` notification when not the active chat.
  - `clearUnread(id)` resets the badge locally when the user opens the chat.
- `app/src/main/java/com/io/lkconsultants/reverb/ReverbManager.kt` — refactored to support **multiple listeners per channel**. Internally a single `PrivateChannelEventListener` dispatcher per channel fans out to a thread-safe set of registered `ChatChannelListener`s. New `removeConversationListener(id, listener)` only tears the channel down once the last listener is gone. This is required because both `UsersViewModel` (for unread/notifications) and `ChatViewModel` (for the open chat) want the same channel — Pusher's `subscribePrivate` only attaches the *first* listener, so the second call would silently no-op without the dispatcher.
- `app/src/main/java/com/io/lkconsultants/viewmodel/ChatViewModel.kt` — `onCleared()` now uses `removeConversationListener` so leaving the chat doesn't unsubscribe the channel that `UsersViewModel` still depends on.
- `app/src/main/java/com/io/lkconsultants/view/ChatScreen.kt` — `DisposableEffect(id)` calls `ChatNotifier.setActive(id)` on enter and `clearActive()` on leave; channel teardown is delegated to `ChatViewModel.onCleared`.
- `app/src/main/java/com/io/lkconsultants/view/UIScreen.kt` (`UserItemdd`) — already accepted `unreadCount`; just wired through.
- `app/src/main/java/com/io/lkconsultants/view/ListOfChatScreen.kt` — passes `user.unread_count` to `UserItemdd` and calls `viewModel.clearUnread(it.id)` on tap; requests `POST_NOTIFICATIONS` permission once on Android 13+ via `rememberLauncherForActivityResult`.

### How notifications fire
1. List screen subscribes to every conversation channel as soon as the list loads.
2. A new `MessageSent` event arrives → `UsersViewModel` decides whether the user is currently in that chat (`ChatNotifier.activeConversationId == conv.id`).
3. If not in that chat (or the app is in the chat list / elsewhere): badge increments AND `ChatNotifier.showNewMessage(...)` posts a system notification.
4. If the user IS in that chat: nothing visible from the list ViewModel; `ChatViewModel`'s listener (the second one bound to the same channel via the dispatcher) appends the message to the open chat.

## Background notifications + reliable unread refresh

### New file
- `app/src/main/java/com/io/lkconsultants/reverb/ChatService.kt` — a foreground service that holds the Reverb websocket alive when no Activity is running. On start it `startForeground()` with a low-priority "Connected to chat" sticky notification, fetches the user's conversations, and subscribes to every channel through the same `ReverbManager` singleton. On `MessageSent` it posts a `ChatNotifier` notification — but only if `AppForegroundTracker.isForeground == false`, so the in-app UI is never duplicated by a notification.

### Modified files
- `AndroidManifest.xml` — added `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` permissions and a `<service android:name=".reverb.ChatService" android:foregroundServiceType="dataSync" />` declaration.
- `ApplicationClass.kt` — registers an `ActivityLifecycleCallbacks` (`AppForegroundTracker`) that increments/decrements on `onActivityStarted` / `onActivityStopped`. Exposes a thread-safe `isForeground: Boolean`. The service consults this to decide whether to surface a notification.
- `MainActivity.kt` — starts `ChatService` from `onCreate` if a token is already saved, and again from inside `LoginScreen.onLoginSuccess` so a fresh session also kicks the service off.
- `viewmodel/UsersViewModel.kt` — no longer posts notifications itself. The list still updates `unread_count` / `last_message` / `updated_at` live via the same listener, but the actual user-facing notification is owned by `ChatService` to avoid double-firing.
- `view/ListOfChatScreen.kt` — added a `LifecycleEventObserver` on `ON_RESUME` that calls `viewModel.fetchUsers()`. This is the safety net: even if the websocket dropped a message (network blip, doze, screen off), unread counts re-sync from the server every time the user lands on the chat list.

### Behavior matrix
| App state                           | Who delivers the update?                           |
| ----------------------------------- | -------------------------------------------------- |
| In chat list                        | `UsersViewModel` updates the badge live.           |
| In active chat                      | `ChatViewModel` appends; badge stays at 0.         |
| App backgrounded (process alive)    | `ChatService` posts a system notification.         |
| App force-stopped / swipe-killed    | Nothing — needs FCM (backend change required).     |
| User returns to chat list           | `ON_RESUME` refetch reconciles unread from server. |

### Caveat
A force-stopped app stops its foreground service too. To deliver notifications in that state we need server-driven push (FCM): the Laravel backend would need to dispatch an FCM message in `MessageSent::__construct` (or via a queued listener), and the Android app needs `FirebaseMessagingService`. That is intentionally **not** included here — it's a backend change.

## Verifying it works

Filter logcat by tag `Reverb`. Expected sequence:
```
STATE DISCONNECTED -> CONNECTING
STATE CONNECTING -> CONNECTED
SUBSCRIBED private-conversation.<id>
EVENT MessageSent -> {"message":{...}}
```

If you see `AUTH FAILED`, the bearer token is rejected or the user isn't a participant of that conversation (channel callback returns `false`).
