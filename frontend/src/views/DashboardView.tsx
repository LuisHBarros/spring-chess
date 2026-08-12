import React from 'react';
import { User, MicroserviceStatus, ViewType, Friend } from '../types';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Play, Trophy, Users, Swords, Activity, ArrowUpRight, Zap, CheckCircle2 } from 'lucide-react';

interface DashboardViewProps {
  currentUser: User;
  services: MicroserviceStatus[];
  friends: Friend[];
  onNavigate: (view: ViewType) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  currentUser,
  services,
  friends,
  onNavigate,
}) => {
  return (
    <div className="space-y-6 pb-12">
      {/* Hero Welcome Banner */}
      <div className="relative overflow-hidden rounded-2xl border border-slate-800 bg-gradient-to-r from-slate-900 via-slate-900/90 to-emerald-950/40 p-6 md:p-8">
        <div className="relative z-10 max-w-2xl space-y-4">
          <div className="inline-flex items-center gap-2 rounded-full bg-emerald-500/10 border border-emerald-500/20 px-3 py-1 text-xs text-emerald-400 font-medium">
            <Zap className="h-3.5 w-3.5" /> High-Performance Microservices Web Gaming Platform
          </div>
          <h2 className="text-3xl md:text-4xl font-extrabold text-white tracking-tight">
            Welcome back, <span className="text-emerald-400">{currentUser.username}</span>!
          </h2>
          <p className="text-sm md:text-base text-slate-300 leading-relaxed">
            Jump directly into an active chess match backed by DDD Game Engine, challenge your friends on the Social Microservice, or monitor system event propagation.
          </p>
          <div className="flex flex-wrap gap-3 pt-2">
            <Button variant="emerald" size="lg" onClick={() => onNavigate('game')} className="gap-2 shadow-lg shadow-emerald-950/60">
              <Swords className="h-5 w-5" /> Jump to Active Game
            </Button>
            <Button variant="secondary" size="lg" onClick={() => onNavigate('lobby')} className="gap-2">
              <Trophy className="h-5 w-5 text-amber-400" /> Matchmaking Lobby
            </Button>
          </div>
        </div>

        {/* Decorative chess piece outline background */}
        <div className="absolute right-4 bottom-[-20px] opacity-10 text-9xl select-none pointer-events-none text-emerald-400 hidden md:block">
          ♟️
        </div>
      </div>

      {/* Overview Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="hover:border-slate-700 transition-all">
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 font-medium">Global Elo Rating</p>
              <h4 className="text-2xl font-bold text-slate-100 mt-1">{currentUser.elo}</h4>
              <p className="text-[11px] text-emerald-400 mt-1 flex items-center gap-1">
                <ArrowUpRight className="h-3 w-3" /> +45 this week
              </p>
            </div>
            <div className="h-11 w-11 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
              <Trophy className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>

        <Card className="hover:border-slate-700 transition-all">
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 font-medium">Win / Loss Ratio</p>
              <h4 className="text-2xl font-bold text-slate-100 mt-1">
                {currentUser.wins}W / {currentUser.losses}L
              </h4>
              <p className="text-[11px] text-slate-400 mt-1">
                {((currentUser.wins / (currentUser.wins + currentUser.losses)) * 100).toFixed(1)}% Win Rate
              </p>
            </div>
            <div className="h-11 w-11 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <Swords className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>

        <Card className="hover:border-slate-700 transition-all">
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 font-medium">Online Friends</p>
              <h4 className="text-2xl font-bold text-slate-100 mt-1">
                {friends.filter(f => f.status !== 'offline').length} / {friends.length}
              </h4>
              <p className="text-[11px] text-slate-400 mt-1">1 currently in-game</p>
            </div>
            <div className="h-11 w-11 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
              <Users className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>

        <Card className="hover:border-slate-700 transition-all">
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 font-medium">Microservices Health</p>
              <h4 className="text-2xl font-bold text-emerald-400 mt-1">100% Operational</h4>
              <p className="text-[11px] text-slate-400 mt-1">5 Microservices Healthy</p>
            </div>
            <div className="h-11 w-11 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <Activity className="h-6 w-6" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Content Grid: Services Status & Friends/Leaderboard */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Services Health Grid */}
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle>Distributed Microservices Status</CardTitle>
                <CardDescription>Real-time health & latency of Spring Boot 3 services</CardDescription>
              </div>
              <Button variant="outline" size="sm" onClick={() => onNavigate('observability')}>
                Open Telemetry Dashboard
              </Button>
            </CardHeader>
            <CardContent className="space-y-3">
              {services.map((svc) => (
                <div
                  key={svc.serviceKey}
                  className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 transition-all"
                >
                  <div className="flex items-center space-x-3">
                    <div className="h-9 w-9 rounded-lg bg-slate-900 flex items-center justify-center border border-slate-800 text-emerald-400">
                      <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                    </div>
                    <div>
                      <div className="font-semibold text-slate-200 text-sm flex items-center gap-2">
                        {svc.name}
                        <Badge variant="outline" className="font-mono text-[10px] text-slate-400">
                          Port :{svc.port}
                        </Badge>
                      </div>
                      <p className="text-xs text-slate-400">{svc.tech}</p>
                    </div>
                  </div>

                  <div className="text-right">
                    <Badge variant="success" className="gap-1">
                      <span className="h-1.5 w-1.5 rounded-full bg-green-400 animate-ping"></span>
                      {svc.status}
                    </Badge>
                    <p className="text-[11px] font-mono text-slate-400 mt-1">{svc.latencyMs}ms latency</p>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar Panel: Online Friends & Quick Play */}
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="justify-between">
                <span>Social Activity</span>
                <Button variant="ghost" size="sm" onClick={() => onNavigate('social')}>
                  View All
                </Button>
              </CardTitle>
              <CardDescription>Friends online and in-match</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {friends.map((friend) => (
                <div key={friend.id} className="flex items-center justify-between p-2.5 rounded-lg bg-slate-950/40 border border-slate-800/80">
                  <div className="flex items-center space-x-3">
                    <div className="relative">
                      <img src={friend.avatar} alt={friend.username} className="h-8 w-8 rounded-lg object-cover" />
                      <span
                        className={`absolute -bottom-0.5 -right-0.5 h-2.5 w-2.5 rounded-full border-2 border-slate-900 ${
                          friend.status === 'online'
                            ? 'bg-emerald-400'
                            : friend.status === 'in_game'
                            ? 'bg-amber-400'
                            : 'bg-slate-500'
                        }`}
                      ></span>
                    </div>
                    <div>
                      <div className="text-xs font-semibold text-slate-200 flex items-center gap-1">
                        {friend.title && <span className="text-[9px] bg-amber-500/20 text-amber-300 px-1 rounded">{friend.title}</span>}
                        {friend.username}
                      </div>
                      <p className="text-[10px] text-slate-400 truncate max-w-[140px]">{friend.currentActivity}</p>
                    </div>
                  </div>

                  <Button variant="outline" size="sm" onClick={() => onNavigate('chat')} className="h-7 px-2 text-xs">
                    Message
                  </Button>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
