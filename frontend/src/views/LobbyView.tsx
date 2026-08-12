import React, { useState } from 'react';
import { User, ViewType } from '../types';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Trophy, Zap, Clock, ShieldAlert, Swords, Search, Plus, Sparkles } from 'lucide-react';

interface LobbyViewProps {
  currentUser: User;
  onStartMatch: (timeControl: string) => void;
  onNavigate: (view: ViewType) => void;
}

export const LobbyView: React.FC<LobbyViewProps> = ({ currentUser, onStartMatch, onNavigate }) => {
  const [isSearching, setIsSearching] = useState(false);
  const [selectedControl, setSelectedControl] = useState('5+0 Rapid');

  const timeControls = [
    { id: '1+0', name: 'Bullet 1+0', icon: Zap, color: 'text-amber-400', desc: '1 minute per player' },
    { id: '3+2', name: 'Blitz 3+2', icon: Trophy, color: 'text-emerald-400', desc: '3 mins + 2s increment' },
    { id: '5+0', name: 'Rapid 5+0', icon: Clock, color: 'text-blue-400', desc: '5 mins standard rapid' },
    { id: '10+0', name: 'Classical 10+0', icon: Swords, color: 'text-purple-400', desc: '10 mins deep calculation' },
  ];

  const openChallenges = [
    { id: 'c1', host: 'Bob_The_King', elo: 2410, title: 'IM', timeControl: '5+0 Rapid', rated: true },
    { id: 'c2', host: 'Charlie_Knight', elo: 1850, timeControl: '3+2 Blitz', rated: true },
    { id: 'c3', host: 'Diana_Queen', elo: 2310, title: 'WGM', timeControl: '1+0 Bullet', rated: true },
  ];

  const handleQuickMatch = (control: string) => {
    setSelectedControl(control);
    setIsSearching(true);
    setTimeout(() => {
      setIsSearching(false);
      onStartMatch(control);
    }, 1800);
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header Banner */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-100">Matchmaking Lobby</h2>
            <Badge variant="default">Game Service :8083</Badge>
          </div>
          <p className="text-sm text-slate-400 mt-1">Select your preferred time control or join an open challenge pool.</p>
        </div>
        <div className="flex items-center space-x-3">
          <Button variant="emerald" onClick={() => handleQuickMatch(selectedControl)} className="gap-2">
            <Sparkles className="h-4 w-4" /> Quick Match (Auto Pairing)
          </Button>
        </div>
      </div>

      {/* Searching Overlay */}
      {isSearching && (
        <Card className="border-emerald-500/50 bg-emerald-950/20 glow-emerald">
          <CardContent className="p-6 text-center space-y-3">
            <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500/20 text-emerald-400 animate-spin">
              <Search className="h-6 w-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-100">Finding Opponent in Pool...</h3>
            <p className="text-xs text-slate-400">Searching for players around {currentUser.elo} Elo • Time Control: {selectedControl}</p>
          </CardContent>
        </Card>
      )}

      {/* Time Control Cards Grid */}
      <div>
        <h3 className="text-sm font-bold text-slate-400 uppercase tracking-wider mb-3">Time Controls</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {timeControls.map((tc) => {
            const Icon = tc.icon;
            return (
              <Card key={tc.id} className="hover:border-emerald-500/40 transition-all cursor-pointer group" onClick={() => handleQuickMatch(tc.name)}>
                <CardContent className="p-5 space-y-3">
                  <div className="flex items-center justify-between">
                    <Icon className={`h-7 w-7 ${tc.color}`} />
                    <Badge variant="outline" className="text-[10px]">Rated</Badge>
                  </div>
                  <div>
                    <h4 className="font-bold text-slate-100 group-hover:text-emerald-400 transition-colors">{tc.name}</h4>
                    <p className="text-xs text-slate-400 mt-0.5">{tc.desc}</p>
                  </div>
                  <Button variant="secondary" size="sm" className="w-full text-xs">
                    Play {tc.id}
                  </Button>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>

      {/* Open Challenges List */}
      <Card>
        <CardHeader>
          <CardTitle>Open Challenge Lobby</CardTitle>
          <CardDescription>Direct room invites waiting for an opponent</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {openChallenges.map((challenge) => (
            <div key={challenge.id} className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700">
              <div className="flex items-center space-x-3">
                <div className="h-9 w-9 rounded-lg bg-slate-900 border border-slate-800 flex items-center justify-center font-bold text-slate-300">
                  ♟️
                </div>
                <div>
                  <div className="text-sm font-semibold text-slate-200 flex items-center gap-1.5">
                    {challenge.title && <span className="bg-amber-500/20 text-amber-300 text-[10px] px-1 rounded">{challenge.title}</span>}
                    {challenge.host} ({challenge.elo})
                  </div>
                  <p className="text-xs text-slate-400">{challenge.timeControl} • Rated Match</p>
                </div>
              </div>

              <Button variant="emerald" size="sm" onClick={() => onStartMatch(challenge.timeControl)}>
                Accept Challenge
              </Button>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
};
