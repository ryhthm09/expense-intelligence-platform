"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  BarChart3,
  LayoutDashboard,
  LogOut,
  Receipt,
  Sparkles,
  Upload,
  Wallet,
} from "lucide-react";
import { useAuth } from "@/lib/auth/auth-context";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/transactions", label: "Transactions", icon: Receipt, disabled: true },
  { href: "/analytics", label: "Analytics", icon: BarChart3, disabled: true },
  { href: "/import", label: "Import", icon: Upload, disabled: true },
  { href: "/insights", label: "AI Insights", icon: Sparkles, disabled: true },
  { href: "/budgets", label: "Budgets", icon: Wallet, disabled: true },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  return (
    <aside className="flex h-full w-64 flex-col border-r border-zinc-200 bg-white">
      <div className="flex items-center gap-2 border-b border-zinc-200 px-6 py-5">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-600">
          <Wallet className="h-4 w-4 text-white" />
        </div>
        <div>
          <p className="text-sm font-semibold text-zinc-900">Expense Intel</p>
          <p className="text-xs text-zinc-500">Financial Intelligence</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map(({ href, label, icon: Icon, disabled }) => (
          <Link
            key={href}
            href={disabled ? "#" : href}
            className={cn(
              "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
              pathname === href
                ? "bg-emerald-50 text-emerald-700"
                : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900",
              disabled && "cursor-not-allowed opacity-40 hover:bg-transparent",
            )}
            onClick={disabled ? (e) => e.preventDefault() : undefined}
          >
            <Icon className="h-4 w-4" />
            {label}
            {disabled && (
              <span className="ml-auto text-[10px] font-normal text-zinc-400">Soon</span>
            )}
          </Link>
        ))}
      </nav>

      <div className="border-t border-zinc-200 p-4">
        {user && (
          <div className="mb-3 px-2">
            <p className="text-sm font-medium text-zinc-900">
              {user.firstName} {user.lastName}
            </p>
            <p className="text-xs text-zinc-500 truncate">{user.email}</p>
          </div>
        )}
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900 transition-colors"
        >
          <LogOut className="h-4 w-4" />
          Sign out
        </button>
      </div>
    </aside>
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-screen bg-zinc-50">
      <Sidebar />
      <main className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-7xl p-8">{children}</div>
      </main>
    </div>
  );
}
