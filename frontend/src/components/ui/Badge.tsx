import React from 'react';
import { cn } from '../../lib/utils';

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'secondary' | 'success' | 'warning' | 'destructive' | 'outline';
}

export const Badge: React.FC<BadgeProps> = ({
  className,
  variant = 'default',
  children,
  ...props
}) => {
  const variants = {
    default: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    secondary: 'bg-slate-800 text-slate-300 border-slate-700',
    success: 'bg-green-500/15 text-green-400 border-green-500/30',
    warning: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
    destructive: 'bg-red-500/15 text-red-400 border-red-500/30',
    outline: 'border-slate-700 text-slate-400',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-md border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
        variants[variant],
        className
      )}
      {...props}
    >
      {children}
    </span>
  );
};
