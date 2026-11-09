import { useEffect, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import {
  MessageCircle,
  Users,
  Bell,
  Plus,
  Search,
  Send,
  LogOut,
  Check,
  CheckCheck,
  Sparkles,
  X,
  ArrowUpRight,
  Smile,
  MoreHorizontal,
  Copy,
  Trash2,
  Zap,
} from "lucide-react";
import { auth, users, requests, chats, token } from "./api";
import { connectRealtime } from "./realtime";
import EmojiPicker from "emoji-picker-react";
import { Toaster, toast } from "sonner";
const colors = [
  "#e1e9ff",
  "#e9e2ff",
  "#dcf3eb",
  "#ffe9dd",
  "#f9e2ed",
  "#e6edf8",
];
const hue = (id) => colors[(Number(id) || 0) % colors.length];

const initial = (n) => (n || "?").slice(0, 1).toUpperCase();
const time = (d) =>
  d
    ? new Date(d).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
    : "";
function Avatar({ name, id, large = false }) {
  return (
    <div
      className={`avatar ${large ? "large" : ""}`}
      style={{ background: hue(id) }}
    >
      {initial(name)}
    </div>
  );
}
function Login({ done }) {
  const [mode, setMode] = useState("login"),
    [form, setForm] = useState({ username: "", email: "", password: "" }),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const change = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      if (mode === "register") await auth.register(form);
      const r = await auth.login({
        email: form.email,
        password: form.password,
      });
      localStorage.setItem("chat_token", r.token);
      done(r.user);
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth">
      <motion.div
        className="auth-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="logo">
          <MessageCircle fill="currentColor" /> Connectify<span>.</span>
        </div>
        <div className="tag">
          <Sparkles size={14} /> YOUR SPACE TO CONNECT
        </div>
        <h1>
          {mode === "login"
            ? "Good conversations start here."
            : "Make room for new connections."}
        </h1>
        <p className="sub">
          A calmer, brighter place to catch up with your people.
        </p>
        <form onSubmit={submit}>
          {mode === "register" && (
            <label>
              Username
              <input
                required
                value={form.username}
                onChange={change("username")}
                placeholder="Your username"
              />
            </label>
          )}
          <label>
            Email address
            <input
              required
              type="email"
              value={form.email}
              onChange={change("email")}
              placeholder="you@example.com"
            />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              value={form.password}
              onChange={change("password")}
              placeholder="Your password"
            />
          </label>
          {error && <div className="error">{error}</div>}
          <button className="primary wide" disabled={busy}>
            {busy
              ? "Please wait…"
              : mode === "login"
                ? "Sign in"
                : "Create account"}{" "}
            <ArrowUpRight size={16} />
          </button>
        </form>
        <p className="switch">
          {mode === "login" ? "New around here?" : "Already have an account?"}
          <button
            onClick={() => setMode(mode === "login" ? "register" : "login")}
          >
            {mode === "login" ? " Create account" : " Sign in"}
          </button>
        </p>
      </motion.div>
      <div className="auth-art">
        <div className="art-bubble bubble1">
          💬 <b>Good vibes only</b>
          <small>Your people, one tap away</small>
        </div>
        <div className="art-center">
          <motion.div
            className="big-mark"
            animate={{ y: [0, -10, 0], rotate: [-8, -4, -8] }}
            transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
            whileHover={{ scale: 1.08, rotateY: 14, rotateX: -10 }}
          >
            <MessageCircle size={55} />
          </motion.div>
          <h2>
            Stay close.
            <br />
            <em>Feel connected.</em>
          </h2>
          <p>Little moments make the best conversations.</p>
        </div>
        <div className="art-bubble bubble2">
          <Avatar name="M" id={2} /> <b>Maya</b>
          <small>That sounds amazing ✨</small>
        </div>
      </div>
    </div>
  );
}
const messageActionStyles = `
.message-content { position: relative; }
.deleted-message { color:#8992a7 !important; background:#f3f4f8 !important; border:1px dashed #d9deea; box-shadow:none !important; }
.deleted-message em { font-size:12px; font-style:italic; }
.message-author-row { display:flex; align-items:center; justify-content:space-between; gap:12px; min-height:20px; }
.message-actions { position:relative; }
.message-more { display:inline-flex; align-items:center; justify-content:center; width:26px; height:24px; border:0; border-radius:7px; background:transparent; color:#8791a8; cursor:pointer; opacity:.72; }
.message-more:hover { background:#eef0ff; color:#5968f5; opacity:1; }
.message-menu { position:absolute; right:0; top:calc(100% + 5px); z-index:30; width:164px; padding:5px; background:#fff; border:1px solid #e8eaf2; border-radius:10px; box-shadow:0 10px 28px rgba(30,40,80,.16); }
.message-menu button { display:flex; align-items:center; gap:9px; width:100%; padding:9px 10px; border:0; border-radius:7px; background:transparent; color:#35405a; font-size:13px; text-align:left; cursor:pointer; }
.message-menu button:hover { background:#f4f5fb; }
.message-menu button.delete-action { color:#d64545; }
.message-menu button:disabled { opacity:.55; cursor:wait; }
`;

export default function App() {
  useEffect(() => {
    document.title = "Connectify";
  }, []);
  const [user, setUser] = useState(null),
    [boot, setBoot] = useState(true),
    [tab, setTab] = useState("chats"),
    [convos, setConvos] = useState([]),
    [people, setPeople] = useState([]),
    [incoming, setIncoming] = useState([]),
    [selected, setSelected] = useState(null),
    [msgs, setMsgs] = useState([]),
    [draft, setDraft] = useState(""),
    [showEmojiPicker, setShowEmojiPicker] = useState(false),
    [showConvMenu, setShowConvMenu] = useState(false),
    [messageMenuId, setMessageMenuId] = useState(null),
    [deletingMessageId, setDeletingMessageId] = useState(null),
    [query, setQuery] = useState(""),
    [found, setFound] = useState([]),
    [modal, setModal] = useState(""),
    [groupName, setGroupName] = useState(""),
    [members, setMembers] = useState([]),
    [status, setStatus] = useState("disconnected"),
    client = useRef(null),
    sub = useRef(null),
    bottom = useRef(null);
  const notify = (message) => toast(message, { duration: 2800 });
  useEffect(() => {
    if (!token()) {
      setBoot(false);
      return;
    }
    auth
      .me()
      .then(setUser)
      .catch(() => localStorage.removeItem("chat_token"))
      .finally(() => setBoot(false));
  }, []);
  async function refresh() {
    try {
      const [a, b, c] = await Promise.all([
        chats.all(),
        users.connections(),
        requests.received(),
      ]);

      // Update conversations, connections and incoming requests
      setConvos(a || []);
      setPeople(b || []);
      setIncoming(c || []);

      // Restore the previously selected conversation
      if (a?.length) {
        const savedChatId = localStorage.getItem("connectify_selected_chat");

        const savedChat = a.find((chat) => String(chat.id) === savedChatId);

        // Open saved conversation, or first conversation if unavailable
        const chatToSelect = savedChat || a[0];

        setSelected(chatToSelect);

        // Update storage in case the previous conversation no longer exists
        if (!savedChat) {
          localStorage.setItem(
            "connectify_selected_chat",
            String(chatToSelect.id),
          );
        }
      } else {
        // No conversations available
        setSelected(null);
        localStorage.removeItem("connectify_selected_chat");
      }
    } catch (e) {
      notify(e.message);
    }
  }
  useEffect(() => {
    if (!user) return;
    refresh();
    client.current = connectRealtime(token(), setStatus);
    return () => {
      sub.current?.unsubscribe();
      client.current?.deactivate();
    };
  }, [user]);
  useEffect(() => {
    if (!selected?.id || status !== "connected" || !client.current) return;
    sub.current?.unsubscribe();
    chats
      .messages(selected.id)
      .then((p) => setMsgs((p?.content || []).slice().reverse()))
      .catch((e) => notify(e.message));
    sub.current = client.current.subscribe(
      `/topic/conversations/${selected.id}`,
      (f) => {
        const m = JSON.parse(f.body);
        setMsgs((old) => (old.some((x) => x.id === m.id) ? old : [...old, m]));
      },
    );
    return () => sub.current?.unsubscribe();
  }, [selected?.id, status]);
  useEffect(() => {
    bottom.current?.scrollIntoView({ behavior: "smooth" });
  }, [msgs]);
  async function search(q) {
    setQuery(q);
    if (!q.trim()) {
      setFound([]);
      return;
    }
    try {
      setFound(await users.search(q));
    } catch (e) {
      notify(e.message);
    }
  }
  async function openChat(p) {
    try {
      const c = await chats.createPrivate(p.id);
      setSelected(c);
      setShowEmojiPicker(false);
      setShowConvMenu(false);
      setTab("chats");
      setModal("");
      await refresh();
    } catch (e) {
      notify(e.message);
    }
  }

  function send(e) {
    e.preventDefault();

    const content = draft.trim();

    if (!content || !selected) return;

    if (!client.current?.connected) {
      console.warn("WebSocket is not connected. Message not sent.");
      notify("Connection interrupted. Please try again.");
      return;
    }

    try {
      client.current.publish({
        destination: `/app/conversations/${selected.id}/sendMessage`,
        body: JSON.stringify({ content }),
      });

      setDraft("");
      setShowEmojiPicker(false);
    } catch (error) {
      console.error("PUBLISH ERROR:", error);
      notify("Unable to send message. Please try again.");
    }
  }

  async function deleteMessage(message) {
    if (!message?.id) {
      notify("This message cannot be deleted because it has no ID.");
      return;
    }

    const mine = message.sender?.email === user.email;

    // DELETE FOR EVERYONE (your own messages)
    if (mine) {
      if (
        !window.confirm(
          "Permanently delete this message? It will be removed for everyone.",
        )
      )
        return;

      setDeletingMessageId(message.id);

      try {
        await chats.deleteMessage(message.id);

        setMsgs((current) =>
          current.map((item) =>
            item.id === message.id
              ? {
                  ...item,
                  deleted: true,
                  content: "This message was deleted",
                }
              : item,
          ),
        );

        setMessageMenuId(null);
        notify("Message permanently deleted");
      } catch (error) {
        notify(error.message || "Unable to delete message");
      } finally {
        setDeletingMessageId(null);
      }

      return;
    }

    // DELETE FOR ME (received messages)
    if (!window.confirm("Delete this message from your view only?")) return;

    setDeletingMessageId(message.id);

    try {
      await chats.hideMessage(message.id);

      setMsgs((current) =>
        current.map((item) =>
          item.id === message.id
            ? { ...item, deletedForCurrentUser: true }
            : item,
        ),
      );

      setMessageMenuId(null);
      notify("Message deleted from your view");
    } catch (error) {
      notify(error.message || "Unable to delete message");
    } finally {
      setDeletingMessageId(null);
    }
  }

  async function copyMessage(content) {
    try {
      await navigator.clipboard.writeText(content);
      setMessageMenuId(null);
      notify("Message copied");
    } catch {
      notify("Unable to copy message");
    }
  }

  async function doRequest(p) {
    try {
      await requests.send(p.id);
      notify("Connection request sent");
      setFound([]);
      setQuery("");
    } catch (e) {
      notify(e.message);
    }
  }
  async function decide(id, ok) {
    try {
      if (ok) await requests.accept(id);
      else await requests.reject(id);
      await refresh();
      notify(ok ? "Connection accepted" : "Request declined");
    } catch (e) {
      notify(e.message);
    }
  }
  async function createGroup(e) {
    e.preventDefault();
    try {
      const c = await chats.createGroup({
        name: groupName,
        memberIds: members,
      });
      setModal("");
      setGroupName("");
      setMembers([]);
      await refresh();
      setSelected(c);
      notify("Group created");
    } catch (e) {
      notify(e.message);
    }
  }
  function logout() {
    localStorage.removeItem("chat_token");
    client.current?.deactivate();
    setUser(null);
    setSelected(null);
  }
  if (boot)
    return (
      <div className="loading">
        <div className="loader" />
        Preparing your chats…
      </div>
    );
  if (!user) return <Login done={setUser} />;
  const getConversationName = (conversation) => {
    if (!conversation) return "";

    if (conversation.type === "GROUP") {
      return conversation.name || "Group chat";
    }

    const otherParticipant = conversation.participants?.find(
      (participant) => participant.id !== user.id,
    );

    return otherParticipant?.username || "Private chat";
  };

  const title = getConversationName(selected);
  return (
    <div className="shell">
      <style>{messageActionStyles}</style>
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <MessageCircle fill="currentColor" />
          </div>
          Connectify<span>.</span>
        </div>
        <div className="workspace">
          <div className="workspace-icon">✦</div>
          <div>
            <b>My workspace</b>
            <small>Personal space</small>
          </div>
          <span>⌄</span>
        </div>
        <small className="label">MENU</small>
        <nav>
          <button
            className={tab === "chats" ? "active" : ""}
            onClick={() => setTab("chats")}
          >
            <MessageCircle />
            Chats <i>{convos.length}</i>
          </button>
          <button
            className={tab === "people" ? "active" : ""}
            onClick={() => setTab("people")}
          >
            <Users />
            People
          </button>
          <button
            className={tab === "requests" ? "active" : ""}
            onClick={() => setTab("requests")}
          >
            <Bell />
            Requests{" "}
            {incoming.length > 0 && <i className="alert">{incoming.length}</i>}
          </button>
        </nav>
        <div className="side-bottom">
          <div className="tip">
            <Sparkles />
            <b>Make every moment count</b>
            <p>Your circle is just getting started.</p>
            <button onClick={() => setModal("people")}>
              Find people <ArrowUpRight size={14} />
            </button>
          </div>
          <div className="profile">
            <Avatar name={user.username} id={user.id} />
            <div>
              <b>{user.username}</b>
              <small>{user.email}</small>
            </div>
            <button onClick={logout} title="Sign out">
              <LogOut size={17} />
            </button>
          </div>
        </div>
      </aside>
      <main>
        <header className="topbar">
          <div>
            <span>Workspace</span>　/　
            <b>
              {tab === "chats"
                ? "Messages"
                : tab === "people"
                  ? "People"
                  : "Requests"}
            </b>
          </div>
          <div className={`live ${status}`}>
            <i />
            {status === "connected"
              ? "Live connection"
              : status === "error"
                ? "Connection issue"
                : "Connecting"}
          </div>
        </header>
        <AnimatePresence mode="wait" initial={false}>
          {tab === "chats" && (
            <motion.div
              key="chats-page"
              className="chat-layout page-motion"
              initial={{ opacity: 0, y: 18, rotateX: -2, scale: 0.99 }}
              animate={{ opacity: 1, y: 0, rotateX: 0, scale: 1 }}
              exit={{ opacity: 0, y: -12, rotateX: 1.5, scale: 0.99 }}
              transition={{ duration: 0.34, ease: [0.22, 1, 0.36, 1] }}
            >
              <section className="chatlist">
                <div className="section-head">
                  <div>
                    <small className="label">YOUR INBOX</small>
                    <h2>
                      Messages <em>✦</em>
                    </h2>
                    <p>Pick up where the good stuff happens.</p>
                  </div>
                  <button className="add" onClick={() => setModal("people")}>
                    <Plus />
                  </button>
                </div>
                <div className="search">
                  <Search />
                  <input
                    placeholder="Search conversations…"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                  />
                </div>
                <div className="chat-items">
                  {convos
                    .filter((c) =>
                      (c.name || `Private chat ${c.id}`)
                        .toLowerCase()
                        .includes(query.toLowerCase()),
                    )
                    .map((c) => (
                      <button
                        key={c.id}
                        className={`chatitem ${selected?.id === c.id ? "chosen" : ""}`}
                        onClick={() => {
                          setSelected(c);
                          localStorage.setItem(
                            "connectify_selected_chat",
                            String(c.id),
                          );
                          setShowEmojiPicker(false);
                          setShowConvMenu(false);
                          setMessageMenuId(null);
                          setMsgs([]);
                        }}
                      >
                        <Avatar name={c.name || "P"} id={c.id} />
                        <div>
                          <b>{getConversationName(c)}</b>
                          <small>
                            {c.type === "GROUP"
                              ? "Group conversation"
                              : "Private conversation"}
                          </small>
                        </div>
                        <span>›</span>
                      </button>
                    ))}
                  {!convos.length && (
                    <div className="blank">
                      No chats yet.
                      <br />
                      Start with someone from your connections.
                    </div>
                  )}
                </div>
                <button
                  className="group-link"
                  onClick={() => setModal("group")}
                >
                  <Users /> Create a group <Plus />
                </button>
              </section>
              <section className="conversation">
                {selected ? (
                  <>
                    <div className="conv-head">
                      <Avatar name={title} id={selected.id} />
                      <div>
                        <b>{title}</b>
                        <small>
                          <i />{" "}
                          {selected.type === "GROUP"
                            ? "Group conversation"
                            : "Private conversation"}
                        </small>
                      </div>
                      <div style={{ position: "relative", marginLeft: "auto" }}>
                        <button
                          className="more"
                          type="button"
                          title="Conversation details"
                          onClick={() => {
                            setShowConvMenu((v) => !v);
                            setShowEmojiPicker(false);
                          }}
                        >
                          <MoreHorizontal />
                        </button>
                        {showConvMenu && (
                          <div
                            style={{
                              position: "absolute",
                              right: 0,
                              top: "calc(100% + 8px)",
                              zIndex: 20,
                              width: 230,
                              maxHeight: 280,
                              overflowY: "auto",
                              padding: 14,
                              background: "#fff",
                              border: "1px solid #e8eaf2",
                              borderRadius: 12,
                              boxShadow: "0 12px 32px rgba(30, 40, 80, .14)",
                            }}
                          >
                            <div
                              style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                marginBottom: 10,
                              }}
                            >
                              <b>Participants</b>
                              <button
                                type="button"
                                onClick={() => setShowConvMenu(false)}
                                title="Close"
                              >
                                <X size={16} />
                              </button>
                            </div>
                            {(selected.participants || []).map((person) => (
                              <div
                                key={person.id}
                                style={{
                                  display: "flex",
                                  alignItems: "center",
                                  gap: 9,
                                  padding: "7px 0",
                                }}
                              >
                                <Avatar name={person.username} id={person.id} />
                                <div>
                                  <div
                                    style={{ fontSize: 13, fontWeight: 600 }}
                                  >
                                    {person.username}
                                    {person.id === user.id ? " (You)" : ""}
                                  </div>
                                  <div
                                    style={{ fontSize: 11, color: "#8791a8" }}
                                  >
                                    {person.email}
                                  </div>
                                </div>
                              </div>
                            ))}
                            {!selected.participants?.length && (
                              <small style={{ color: "#8791a8" }}>
                                Participant details are unavailable.
                              </small>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="messages">
                      <div className="day">YOUR CONVERSATION</div>
                      {!msgs.length && (
                        <div className="empty">
                          <div>
                            <Sparkles />
                          </div>
                          <h3>Say hello, {user.username}!</h3>
                          <p>
                            This is the beginning of something good.
                            <br />
                            Send the first message to get things moving.
                          </p>
                        </div>
                      )}
                      {msgs.map((m, i) => {
                        const mine = m.sender?.email === user.email;
                        return (
                          <motion.div
                            key={m.id || i}
                            initial={{ opacity: 0, y: 12, scale: 0.985 }}
                            animate={{ opacity: 1, y: 0, scale: 1 }}
                            transition={{
                              duration: 0.22,
                              ease: [0.22, 1, 0.36, 1],
                            }}
                            className={`msg ${mine ? "mine" : ""}`}
                          >
                            {!mine && (
                              <Avatar
                                name={m.sender?.username}
                                id={m.sender?.id}
                              />
                            )}
                            <div className="message-content">
                              <div className="message-author-row">
                                <small>
                                  {mine ? "You" : m.sender?.username}
                                </small>
                                {!m.deleted && !m.deletedForCurrentUser && (
                                  <div className="message-actions">
                                    <button
                                      type="button"
                                      className="message-more"
                                      title="Message options"
                                      aria-label="Message options"
                                      onClick={() =>
                                        setMessageMenuId((current) =>
                                          current === m.id ? null : m.id,
                                        )
                                      }
                                    >
                                      <MoreHorizontal size={16} />
                                    </button>
                                    {messageMenuId === m.id && (
                                      <div className="message-menu">
                                        <button
                                          type="button"
                                          onClick={() => copyMessage(m.content)}
                                        >
                                          <Copy size={14} /> Copy message
                                        </button>
                                        <button
                                          type="button"
                                          className="delete-action"
                                          disabled={deletingMessageId === m.id}
                                          onClick={() => deleteMessage(m)}
                                        >
                                          <Trash2 size={14} />{" "}
                                          {mine
                                            ? deletingMessageId === m.id
                                              ? "Deleting…"
                                              : "Delete for everyone"
                                            : "Delete for me"}
                                        </button>
                                      </div>
                                    )}
                                  </div>
                                )}
                              </div>
                              {m.deleted || m.deletedForCurrentUser ? (
                                <div className="bubble deleted-message">
                                  <em>
                                    {m.deletedForCurrentUser
                                      ? "You deleted this message"
                                      : "This message was deleted"}
                                  </em>
                                </div>
                              ) : (
                                <div className="bubble">{m.content}</div>
                              )}
                              <time>
                                {time(m.sentAt)}{" "}
                                {mine && !m.deletedForCurrentUser && (
                                  <CheckCheck size={13} />
                                )}
                              </time>
                            </div>
                          </motion.div>
                        );
                      })}
                      <div ref={bottom} />
                    </div>
                    <form className="composer" onSubmit={send}>
                      <div className="tools">
                        <div
                          style={{
                            position: "relative",
                            display: "inline-flex",
                          }}
                        >
                          <button
                            type="button"
                            title="Choose emoji"
                            aria-label="Choose emoji"
                            aria-expanded={showEmojiPicker}
                            onClick={() => {
                              setShowEmojiPicker((v) => !v);
                              setShowConvMenu(false);
                            }}
                          >
                            <Smile />
                          </button>
                          {showEmojiPicker && (
                            <div
                              role="dialog"
                              aria-label="Emoji picker"
                              style={{
                                position: "absolute",
                                left: 0,
                                bottom: "calc(100% + 10px)",
                                zIndex: 1000,
                                width: "min(390px, calc(100vw - 32px))",
                                border: "1px solid #e8eaf3",
                                borderRadius: 18,
                                overflow: "hidden",
                                background: "#ffffff",
                                boxShadow: "0 18px 48px rgba(30, 40, 80, .16)",
                              }}
                            >
                              <EmojiPicker
                                onEmojiClick={(emojiData) => {
                                  setDraft((value) => value + emojiData.emoji);
                                }}
                                width="100%"
                                height={420}
                                emojiSize={38}
                                previewConfig={{ showPreview: false }}
                                searchPlaceHolder="Search emojis…"
                                suggestedEmojisMode="recent"
                                skinTonesDisabled={false}
                                lazyLoadEmojis={true}
                                emojiStyle="native"
                                theme="dark"
                              />
                            </div>
                          )}
                        </div>

                        <small>Enter ↵ to send</small>
                      </div>
                      <div className="write">
                        <input
                          value={draft}
                          onChange={(e) => setDraft(e.target.value)}
                          placeholder={`Message ${title}…`}
                        />
                        <button
                          type="submit"
                          disabled={
                            !draft.trim() ||
                            !selected ||
                            !client.current?.connected
                          }
                          title="Send message"
                        >
                          <Send />
                        </button>
                      </div>
                    </form>
                  </>
                ) : (
                  <div className="welcome">
                    <div className="welcome-icon">
                      <MessageCircle />
                    </div>
                    <h2>A little hello goes a long way.</h2>
                    <p>
                      Choose a conversation or start a new one with your people.
                    </p>
                    <button
                      className="primary"
                      onClick={() => setModal("people")}
                    >
                      Start a conversation <ArrowUpRight />
                    </button>
                  </div>
                )}
              </section>
            </motion.div>
          )}
          {tab === "people" && (
            <motion.section
              key="people-page"
              className="page page-motion"
              initial={{ opacity: 0, y: 18, rotateX: -2, scale: 0.99 }}
              animate={{ opacity: 1, y: 0, rotateX: 0, scale: 1 }}
              exit={{ opacity: 0, y: -12, rotateX: 1.5, scale: 0.99 }}
              transition={{ duration: 0.34, ease: [0.22, 1, 0.36, 1] }}
            >
              <small className="label">YOUR CIRCLE</small>
              <h2>
                People <em>✦</em>
              </h2>
              <p>Find your people and make a connection.</p>
              <div className="search people-search">
                <Search />
                <input
                  placeholder="Search by username…"
                  value={query}
                  onChange={(e) => search(e.target.value)}
                />
              </div>
              <div className="cards">
                {found
                  .filter((p) => !people.some((person) => person.id === p.id))
                  .map((p) => (
                    <Person
                      key={p.id}
                      p={p}
                      connect={() => doRequest(p)}
                      chat={() => openChat(p)}
                    />
                  ))}

                {people.map((p) => (
                  <Person
                    key={`c${p.id}`}
                    p={p}
                    chat={() => openChat(p)}
                    connected
                  />
                ))}
              </div>
            </motion.section>
          )}
          {tab === "requests" && (
            <motion.section
              key="requests-page"
              className="page page-motion"
              initial={{ opacity: 0, y: 18, rotateX: -2, scale: 0.99 }}
              animate={{ opacity: 1, y: 0, rotateX: 0, scale: 1 }}
              exit={{ opacity: 0, y: -12, rotateX: 1.5, scale: 0.99 }}
              transition={{ duration: 0.34, ease: [0.22, 1, 0.36, 1] }}
            >
              <small className="label">NEW CONNECTIONS</small>
              <h2>
                Requests <em>✦</em>
              </h2>
              <p>Good things can start with a simple yes.</p>
              <div className="request-list">
                {incoming.map((r) => (
                  <div className="request" key={r.id}>
                    <Avatar name={r.sender?.username} id={r.sender?.id} />
                    <div>
                      <b>{r.sender?.username}</b>
                      <small>{r.sender?.email} wants to connect.</small>
                    </div>
                    <button
                      className="accept"
                      onClick={() => decide(r.id, true)}
                    >
                      <Check /> Accept
                    </button>
                    <button
                      className="decline"
                      onClick={() => decide(r.id, false)}
                    >
                      Decline
                    </button>
                  </div>
                ))}
                {!incoming.length && (
                  <div className="blank">
                    You're all caught up. New requests will appear here.
                  </div>
                )}
              </div>
            </motion.section>
          )}
        </AnimatePresence>
      </main>
      <AnimatePresence>
        {modal && (
          <motion.div
            className="backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setModal("")}
          >
            <motion.div
              className="modal"
              initial={{ y: 18, scale: 0.975, opacity: 0 }}
              animate={{ y: 0, scale: 1, opacity: 1 }}
              exit={{ y: 10, scale: 0.985, opacity: 0 }}
              transition={{ type: "spring", stiffness: 340, damping: 30 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="modal-head">
                <div>
                  <h3>
                    {modal === "group"
                      ? "Create a group"
                      : "Start a conversation"}
                  </h3>
                  <p>
                    {modal === "group"
                      ? "Bring your favorite people together."
                      : "Search for someone by username."}
                  </p>
                </div>
                <button onClick={() => setModal("")}>
                  <X />
                </button>
              </div>
              {modal === "people" ? (
                <>
                  <div className="search">
                    <Search />
                    <input
                      autoFocus
                      placeholder="Search username…"
                      value={query}
                      onChange={(e) => search(e.target.value)}
                    />
                  </div>
                  {found.map((p) => (
                    <div className="result" key={p.id}>
                      <Avatar name={p.username} id={p.id} />
                      <div>
                        <b>{p.username}</b>
                        <small>{p.email}</small>
                      </div>
                      {people.some((person) => person.id === p.id) ? (
                        <button onClick={() => openChat(p)}>Message</button>
                      ) : (
                        <button
                          className="muted-btn"
                          onClick={() => doRequest(p)}
                        >
                          Connect
                        </button>
                      )}
                    </div>
                  ))}
                </>
              ) : (
                <form onSubmit={createGroup}>
                  <label>
                    Group name
                    <input
                      required
                      value={groupName}
                      onChange={(e) => setGroupName(e.target.value)}
                      placeholder="e.g. Weekend plans"
                    />
                  </label>
                  <p className="form-label">Choose connections</p>
                  {people.map((p) => (
                    <label className="member" key={p.id}>
                      <input
                        type="checkbox"
                        checked={members.includes(p.id)}
                        onChange={(e) =>
                          setMembers((v) =>
                            e.target.checked
                              ? [...v, p.id]
                              : v.filter((x) => x !== p.id),
                          )
                        }
                      />
                      <Avatar name={p.username} id={p.id} />
                      {p.username}
                    </label>
                  ))}
                  <button className="primary wide">
                    Create group <ArrowUpRight />
                  </button>
                </form>
              )}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
      <Toaster
        position="bottom-center"
        theme="dark"
        richColors
        closeButton
        duration={2800}
        toastOptions={{
          style: {
            background: "#171c2b",
            border: "1px solid #303951",
            color: "#f4f6ff",
            borderRadius: "14px",
          },
        }}
      />
    </div>
  );
}
function Person({ p, connect, chat, connected }) {
  return (
    <motion.div className="person" whileHover={{ y: -3 }}>
      <Avatar name={p.username} id={p.id} large />
      <div>
        <b>{p.username}</b>
        <small>{p.email}</small>
      </div>
      {connected ? (
        <button onClick={chat}>Message</button>
      ) : (
        <button onClick={connect}>＋ Connect</button>
      )}
    </motion.div>
  );
}
