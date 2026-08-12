import React from 'react';
import { User, MicroserviceStatus, ViewType } from '../types';
import { Badge } from './ui/Badge';
import { Button } from './ui/Button';
import { ShieldCheck, Activity, Users, MessageSquare, Play, UserCheck, ChevronDown } from 'lucide-react';

interface NavbarProps {
  currentUser: User;
  allUsers: User[];
  onSelectUser: (user: User) => void;
  services: MicroserviceStatus[];
  activeView: ViewType;
  onNavigate: (view: ViewType) => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentUser,
  allUsers,
  onSelectUser,
  services,
  onNavigate,
}) => {
  const [userDropdownOpen, setUserDropdownOpen] = React.useState(false);

  return (
    <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-slate-950/80 backdrop-blur-md px-4 py-3">
      <div className="flex items-center justify-between max-w-7xl mx-auto">
        {/* Brand logo & Title */}
        <div className="flex items-center space-x-3 cursor-pointer" onClick={() => onNavigate('dashboard')}>
          <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 p-0.5 shadow-lg shadow-emerald-950/50 flex items-center justify-center">
            <span className="text-2xl">♟️</span>
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-lg font-bold tracking-tight text-white">Spring Chess</h1>
              <Badge variant="default" className="text-[10px] py-0">v1.0 Microservices</Badge>
            </div>
            <p className="text-xs text-slate-400">DDD & Multi-Service Gaming Architecture</p>
          </div>
        </div>

        {/* Microservices Status Bar */}
        <div className="hidden lg:flex items-center space-x-2 bg-slate-900/90 rounded-xl p-1.5 border border-slate-800">
          <span className="text-xs text-slate-400 font-medium px-2 flex items-center gap-1">
            <Activity className="h-3.5 w-3.5 text-emerald-400 animate-pulse" /> Architecture Health:
          </span>
          {services.map((svc) => (
            <div
              key={svc.serviceKey}
              className="flex items-center gap-1.5 px-2 py-1 rounded-lg bg-slate-950/60 border border-slate-800 text-xs"
              title={`${svc.name} - ${svc.tech} (${svc.dbType})`}
            >
              <span className="h-2 w-2 rounded-full bg-emerald-400 glow-emerald"></span>
              <span className="font-mono text-slate-300 capitalize">{svc.serviceKey}</span>
              <span className="text-[10px] text-slate-500 font-mono">:{svc.port}</span>
            </div>
          ))}
        </div>

        {/* User Account Switcher & Actions */}
        <div className="flex items-center space-x-3">
          <Button variant="emerald" size="sm" onClick={() => onNavigate('lobby')} className="gap-1.5">
            <Play className="h-4 w-4 fill-slate-950" /> Play Game
          </Button>

          {/* User selector dropdown */}
          <div className="relative">
            <button
              onClick={() => setUserDropdownOpen(!userDropdownOpen)}
              className="flex items-center space-x-2.5 p-1.5 pr-3 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 transition-all text-left"
            >
              <img
                src={currentUser.avatar}
                alt={currentUser.username}
                className="h-8 w-8 rounded-lg object-cover ring-2 ring-emerald-500/40"
              />
              <div className="hidden sm:block">
                <div className="flex items-center gap-1 text-xs font-semibold text-slate-100">
                  {currentUser.title && (
                    <span className="bg-amber-500/20 text-amber-300 text-[10px] px-1 rounded font-bold">
                      {currentUser.title}
                    </span>
                  )}
                  {currentUser.username}
                </div>
                <div className="text-[11px] text-slate-400 font-mono">Rating: {currentUser.elo}</div>
              </div>
              <ChevronDown className="h-4 w-4 text-slate-400" />
            </button>

            {userDropdownOpen && (
              <div className="absolute right-0 mt-2 w-64 rounded-xl bg-slate-900 border border-slate-800 shadow-2xl p-2 z-50 animate-in fade-in slide-in-from-top-2">
                <div className="px-3 py-2 border-b border-slate-800 mb-1">
                  <p className="text-xs font-medium text-slate-400">Switch Active User (Auth Context)</p>
                </div>
                {allUsers.map((u) => (
                  <button
                    key={u.id}
                    onClick={() => {
                      onSelectUser(u);
                      setUserDropdownOpen(false);
                    }}
                    className={`w-full flex items-center justify-between p-2 rounded-lg text-left text-xs transition-colors ${
                      u.id === currentUser.id
                        ? 'bg-emerald-500/10 text-emerald-300 font-medium'
                        : 'hover:bg-slate-800 text-slate-300'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <img src={u.avatar} className="h-6 w-6 rounded-md object-cover" alt="" />
                      <div>
                        <div className="font-semibold text-slate-200">{u.username}</div>
                        <div className="text-[10px] text-slate-400">{u.elo} Elo • {u.status}</div>
                      </div>
                    </div>
                    {u.id === currentUser.id && <UserCheck className="h-4 w-4 text-emerald-400" />}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
