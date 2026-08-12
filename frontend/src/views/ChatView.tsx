import React, { useState } from 'react';
import { User, ChatMessage } from '../types';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { MessageSquare, Send, Hash, Lock, Users, Sparkles } from 'lucide-react';

interface ChatViewProps {
  currentUser: User;
  messages: ChatMessage[];
  onSendMessage: (channelId: string, text: string) => void;
}

export const ChatView: React.FC<ChatViewProps> = ({ currentUser, messages, onSendMessage }) => {
  const [activeChannel, setActiveChannel] = useState<'global' | 'match' | 'guild'>('global');
  const [inputText, setInputText] = useState('');

  const channels = [
    { id: 'global', name: 'global-lobby', icon: Hash, desc: 'Public lobby chat for all connected players' },
    { id: 'match', name: 'match-live-chat', icon: Sparkles, desc: 'Real-time messaging for active chess game' },
    { id: 'guild', name: 'gme-guild-lounge', icon: Users, desc: 'Grandmaster Elite guild members channel' },
  ];

  const filteredMessages = messages.filter(
    (m) => m.channelId === activeChannel || (activeChannel === 'match' && m.channelId === 'match')
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim()) return;
    onSendMessage(activeChannel, inputText.trim());
    setInputText('');
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header Banner */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-100">Chat Microservice Hub</h2>
            <Badge variant="default">Chat Service :8084</Badge>
          </div>
          <p className="text-sm text-slate-400 mt-1">Real-Time Messaging with WebSocket & Redis pub/sub backplane.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Channel Selector Sidebar */}
        <div className="lg:col-span-4 space-y-3">
          <Card>
            <CardHeader className="py-3 px-4">
              <CardTitle className="text-xs uppercase tracking-wider text-slate-400">Channels & Direct Messages</CardTitle>
            </CardHeader>
            <CardContent className="p-2 space-y-1">
              {channels.map((ch) => {
                const Icon = ch.icon;
                const isActive = activeChannel === ch.id;
                return (
                  <button
                    key={ch.id}
                    onClick={() => setActiveChannel(ch.id as any)}
                    className={`w-full flex items-center justify-between p-3 rounded-xl text-left text-xs font-semibold transition-all ${
                      isActive
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                        : 'hover:bg-slate-800 text-slate-300'
                    }`}
                  >
                    <div className="flex items-center space-x-2.5">
                      <Icon className="h-4 w-4" />
                      <span>#{ch.name}</span>
                    </div>
                    {isActive && <span className="h-2 w-2 rounded-full bg-emerald-400 glow-emerald"></span>}
                  </button>
                );
              })}
            </CardContent>
          </Card>
        </div>

        {/* Chat Feed & Message Composer */}
        <div className="lg:col-span-8">
          <Card className="flex flex-col h-[520px]">
            <CardHeader className="py-3.5 px-5 border-b border-slate-800">
              <CardTitle className="text-base flex items-center gap-2">
                <Hash className="h-5 w-5 text-emerald-400" /> #{channels.find((c) => c.id === activeChannel)?.name}
              </CardTitle>
            </CardHeader>

            <CardContent className="p-5 flex-1 flex flex-col justify-between overflow-hidden">
              {/* Message Feed */}
              <div className="overflow-y-auto space-y-3 pr-2 flex-1">
                {filteredMessages.map((msg) => (
                  <div key={msg.id} className="flex items-start space-x-3">
                    {msg.senderAvatar ? (
                      <img src={msg.senderAvatar} alt="" className="h-8 w-8 rounded-lg object-cover ring-1 ring-slate-800 mt-0.5" />
                    ) : (
                      <div className="h-8 w-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-xs">
                        SYS
                      </div>
                    )}
                    <div className="flex-1 bg-slate-950/60 p-3 rounded-xl border border-slate-800/80">
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-xs text-slate-200">{msg.senderName}</span>
                        <span className="text-[10px] text-slate-500 font-mono">{msg.timestamp}</span>
                      </div>
                      <p className="text-xs text-slate-300 mt-1 leading-relaxed">{msg.text}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* Message Composer */}
              <form onSubmit={handleSubmit} className="flex gap-2 pt-3 border-t border-slate-800 mt-2">
                <input
                  type="text"
                  placeholder={`Message #${channels.find((c) => c.id === activeChannel)?.name}...`}
                  value={inputText}
                  onChange={(e) => setInputText(e.target.value)}
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
                />
                <Button type="submit" variant="emerald" size="md" className="gap-1.5 px-5">
                  <Send className="h-4 w-4" /> Send
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
