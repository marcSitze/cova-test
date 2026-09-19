import React from 'react';
import { useAuth } from '@/context/AuthContext';
import { CheckSquare, LogOut, User } from 'lucide-react';
import { Button } from '@/components/common/Button';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-40 w-full glass-panel border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center shadow-lg shadow-indigo-500/20">
            <CheckSquare className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-100 tracking-tight">Task Manager</h1>
            <p className="text-[10px] text-slate-400 font-medium">Senior Full-Stack Architecture</p>
          </div>
        </div>

        {user && (
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-300">
              <User className="w-3.5 h-3.5 text-indigo-400" />
              <span className="font-medium">{user.email}</span>
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={logout}
              icon={<LogOut className="w-3.5 h-3.5" />}
            >
              Déconnexion
            </Button>
          </div>
        )}
      </div>
    </header>
  );
};
