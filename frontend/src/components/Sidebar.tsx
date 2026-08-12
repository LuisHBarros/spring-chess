import React from 'react';
import { ViewType } from '../types';
import { LayoutDashboard, Swords, Users, MessageSquare, Activity, Trophy } from 'lucide-react';

interface SidebarProps {
  activeView: ViewType;
  onNavigate: (view: ViewType) => void;
  unreadCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({ activeView, onNavigate, unreadCount = 2 }) => {
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'lobby', label: 'Matchmaking Lobby', icon: Trophy },
    { id: 'game', label: 'Chess Match', icon: Swords },
    { id: 'social', label: 'Social & Guilds', icon: Users },
    { id: 'chat', label: 'Chat Hub', icon: MessageSquare, badge: unreadCount },
    { id: 'observability', label: 'Telemetry & Logs', icon: Activity },
  ];

  return (
    <aside className="w-64 shrink-0 border-r border-slate-800 bg-slate-950/60 p-4 hidden md:block flex flex-col justify-between">
      <div className="space-y-6">
        <div>
          <p className="px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
            Platform Services
          </p>
          <nav className="space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeView === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => onNavigate(item.id as ViewType)}
                  className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl font-medium text-sm transition-all duration-150 ${
                    isActive
                      ? 'bg-gradient-to-r from-emerald-600/20 to-teal-600/10 text-emerald-400 border border-emerald-500/30 font-semibold shadow-inner'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-slate-900/80'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <Icon className={`h-4 w-4 ${isActive ? 'text-emerald-400' : 'text-slate-400'}`} />
                    <span>{item.label}</span>
                  </div>
                  {item.badge ? (
                    <span className="bg-emerald-500 text-slate-950 text-[10px] font-bold px-1.5 py-0.5 rounded-full">
                      {item.badge}
                    </span>
                  ) : null}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Microservices Quick Map Card */}
        <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-3.5 text-xs text-slate-400 space-y-2">
          <div className="font-semibold text-slate-300 flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full bg-emerald-400"></span>
            Spring Boot Microservices
          </div>
          <p className="text-[11px] leading-relaxed text-slate-400">
            Powered by DDD Spring Boot 3 services with LocalStack SNS/SQS, PostgreSQL, and DynamoDB.
          </p>
        </div>
      </div>

      <div className="pt-4 border-t border-slate-800 text-[11px] text-slate-500 text-center">
        Chess DDD Engine • React 19 + shadcn/ui
      </div>
    </aside>
  );
};
