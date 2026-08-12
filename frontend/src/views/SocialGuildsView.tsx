import React, { useState } from 'react';
import { User, Friend, Guild, ViewType } from '../types';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Users, Shield, UserPlus, Swords, MessageSquare, Crown, Award, Check } from 'lucide-react';

interface SocialGuildsViewProps {
  currentUser: User;
  friends: Friend[];
  guilds: Guild[];
  onNavigate: (view: ViewType) => void;
  onChallengeFriend: (friendName: string) => void;
}

export const SocialGuildsView: React.FC<SocialGuildsViewProps> = ({
  currentUser,
  friends,
  guilds,
  onNavigate,
  onChallengeFriend,
}) => {
  const [activeTab, setActiveTab] = useState<'friends' | 'guilds'>('friends');
  const [joinedGuildId, setJoinedGuildId] = useState<string>('gld_01');
  const [friendInput, setFriendInput] = useState('');
  const [friendList, setFriendList] = useState<Friend[]>(friends);

  const handleAddFriend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!friendInput.trim()) return;

    const newFriend: Friend = {
      id: `usr_${Date.now()}`,
      username: friendInput.trim(),
      avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
      elo: 1600,
      status: 'online',
      currentActivity: 'Just added',
    };

    setFriendList([newFriend, ...friendList]);
    setFriendInput('');
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header Banner */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-100">Social Interactions & Guilds</h2>
            <Badge variant="default">Social Service :8082</Badge>
          </div>
          <p className="text-sm text-slate-400 mt-1">Manage your friends, guild memberships, and clan standings.</p>
        </div>

        {/* Tab Selector */}
        <div className="flex items-center p-1 rounded-xl bg-slate-950 border border-slate-800">
          <button
            onClick={() => setActiveTab('friends')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'friends' ? 'bg-emerald-600 text-white shadow-sm' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Friends ({friendList.length})
          </button>
          <button
            onClick={() => setActiveTab('guilds')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'guilds' ? 'bg-emerald-600 text-white shadow-sm' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Guilds Directory ({guilds.length})
          </button>
        </div>
      </div>

      {activeTab === 'friends' ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Friends List Column */}
          <div className="lg:col-span-2 space-y-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <CardTitle>Friends List</CardTitle>
                  <CardDescription>Real-time presence and match status</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                {friendList.map((f) => (
                  <div key={f.id} className="flex flex-wrap items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 gap-3">
                    <div className="flex items-center space-x-3">
                      <div className="relative">
                        <img src={f.avatar} alt="" className="h-10 w-10 rounded-xl object-cover ring-1 ring-slate-700" />
                        <span className={`absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-full border-2 border-slate-950 ${
                          f.status === 'online' ? 'bg-emerald-400' : f.status === 'in_game' ? 'bg-amber-400' : 'bg-slate-500'
                        }`}></span>
                      </div>
                      <div>
                        <div className="font-semibold text-slate-200 text-sm flex items-center gap-1.5">
                          {f.title && <span className="bg-amber-500/20 text-amber-300 text-[10px] px-1 rounded">{f.title}</span>}
                          {f.username}
                          <Badge variant="outline" className="text-[10px] font-mono">{f.elo} Elo</Badge>
                        </div>
                        <p className="text-xs text-slate-400 mt-0.5">{f.currentActivity}</p>
                      </div>
                    </div>

                    <div className="flex items-center space-x-2">
                      <Button variant="outline" size="sm" onClick={() => onChallengeFriend(f.username)} className="gap-1 text-xs">
                        <Swords className="h-3.5 w-3.5 text-emerald-400" /> Challenge
                      </Button>
                      <Button variant="secondary" size="sm" onClick={() => onNavigate('chat')} className="gap-1 text-xs">
                        <MessageSquare className="h-3.5 w-3.5" /> Chat
                      </Button>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

          {/* Add Friend Form */}
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Add New Friend</CardTitle>
                <CardDescription>Send friend invite by username</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleAddFriend} className="space-y-3">
                  <input
                    type="text"
                    placeholder="Enter player username..."
                    value={friendInput}
                    onChange={(e) => setFriendInput(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
                  />
                  <Button type="submit" variant="emerald" size="sm" className="w-full gap-1.5">
                    <UserPlus className="h-4 w-4" /> Send Request
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>
        </div>
      ) : (
        /* Guilds Directory */
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {guilds.map((guild) => {
            const isJoined = joinedGuildId === guild.id;
            return (
              <Card key={guild.id} className="overflow-hidden border-slate-800 hover:border-slate-700 transition-all">
                <div className="h-28 relative">
                  <img src={guild.banner} alt="" className="w-full h-full object-cover opacity-60" />
                  <div className="absolute inset-0 bg-gradient-to-t from-slate-900 to-transparent"></div>
                  <div className="absolute top-3 right-3">
                    <Badge variant="warning" className="gap-1">
                      <Crown className="h-3 w-3" /> Rank #{guild.rank}
                    </Badge>
                  </div>
                </div>

                <CardContent className="p-5 pt-2 space-y-4">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="text-lg font-bold text-slate-100">[{guild.tag}] {guild.name}</h3>
                        <Badge variant="outline">Lvl {guild.level}</Badge>
                      </div>
                      <p className="text-xs text-slate-400 mt-1">{guild.description}</p>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-xs text-slate-400 border-t border-b border-slate-800/80 py-2.5">
                    <div>
                      <span className="text-slate-500">Leader:</span> <span className="text-slate-200 font-semibold">{guild.leader}</span>
                    </div>
                    <div>
                      <span className="text-slate-500">Members:</span> <span className="text-slate-200 font-semibold">{guild.membersCount}/200</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between pt-1">
                    {isJoined ? (
                      <Badge variant="success" className="gap-1 text-xs py-1.5 px-3">
                        <Check className="h-3.5 w-3.5" /> Joined Guild
                      </Badge>
                    ) : (
                      <Button variant="emerald" size="sm" onClick={() => setJoinedGuildId(guild.id)}>
                        Join Guild
                      </Button>
                    )}
                    <Button variant="ghost" size="sm" onClick={() => onNavigate('chat')}>
                      Guild Chat
                    </Button>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
};
