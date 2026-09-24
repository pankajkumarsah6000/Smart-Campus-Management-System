import { useEffect, useRef, useState } from 'react';
import { Send, Sparkles } from 'lucide-react';
import { aiService } from '../../services/aiService';

const SUGGESTIONS = [
  "What's my attendance percentage?",
  "What classes do I have today?",
  'Any pending assignments?',
  'Summarize my recent performance',
];

export default function AIAssistant() {
  const [messages, setMessages] = useState([
    { role: 'assistant', text: "Hi! I'm your campus assistant. Ask me about your timetable, attendance, assignments, or notices." },
  ]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [sessionId, setSessionId] = useState(null);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = async (text) => {
    const message = text ?? input;
    if (!message.trim() || sending) return;
    setMessages((m) => [...m, { role: 'user', text: message }]);
    setInput('');
    setSending(true);
    try {
      const res = await aiService.chat(message, sessionId);
      setSessionId(res.sessionId);
      setMessages((m) => [...m, { role: 'assistant', text: res.reply, intent: res.intent }]);
    } catch (err) {
      setMessages((m) => [...m, { role: 'assistant', text: 'Sorry, I ran into an error reaching the assistant. Please try again.' }]);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)]">
      <div className="mb-4">
        <p className="ledger-index uppercase text-ink-muted">Student · AI Assistant</p>
        <h1 className="font-display text-2xl text-ink dark:text-white flex items-center gap-2">
          <Sparkles size={20} className="text-[var(--color-gold)]" /> Campus Assistant
        </h1>
      </div>

      <div className="flex-1 overflow-y-auto rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 p-4 space-y-3">
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] rounded-xl px-4 py-2.5 text-sm ${
              m.role === 'user' ? 'bg-navy-900 text-white' : 'bg-navy-900/5 dark:bg-white/5 text-ink dark:text-white'
            }`}>
              {m.text}
            </div>
          </div>
        ))}
        {sending && (
          <div className="flex justify-start">
            <div className="rounded-xl px-4 py-2.5 text-sm bg-navy-900/5 dark:bg-white/5 text-ink-muted">Thinking…</div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {messages.length <= 1 && (
        <div className="flex flex-wrap gap-2 mt-3">
          {SUGGESTIONS.map((s) => (
            <button key={s} onClick={() => send(s)} className="text-xs px-3 py-1.5 rounded-full border border-black/10 dark:border-white/15 hover:bg-navy-900/5 dark:hover:bg-white/5">
              {s}
            </button>
          ))}
        </div>
      )}

      <form onSubmit={(e) => { e.preventDefault(); send(); }} className="flex items-center gap-2 mt-3">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask about your timetable, attendance, assignments…"
          className="flex-1 rounded-lg border border-black/10 dark:border-white/15 bg-white dark:bg-navy-800 px-3.5 py-2.5 text-sm outline-none focus:border-navy-700"
        />
        <button type="submit" disabled={sending} aria-label="Send message" className="p-2.5 rounded-lg bg-navy-900 text-white hover:bg-navy-800 disabled:opacity-60">
          <Send size={16} />
        </button>
      </form>
    </div>
  );
}
