"use client";

import { BarChart3, Receipt, Sparkles, Upload } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/protected-route";
import { AppShell } from "@/components/layout/sidebar";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/lib/auth/auth-context";

const upcomingFeatures = [
  {
    icon: Receipt,
    title: "Transactions",
    phase: "Phase 2",
    description: "Create, edit, and filter your transactions",
  },
  {
    icon: Upload,
    title: "CSV Import",
    phase: "Phase 3",
    description: "Import from Chase, BofA, Wells Fargo, and more",
  },
  {
    icon: BarChart3,
    title: "Analytics",
    phase: "Phase 4",
    description: "Spending trends, category breakdowns, and charts",
  },
  {
    icon: Sparkles,
    title: "AI Insights",
    phase: "Phase 5",
    description: "Natural language queries and smart recommendations",
  },
];

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <DashboardContent />
    </ProtectedRoute>
  );
}

function DashboardContent() {
  const { user } = useAuth();

  return (
    <AppShell>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-zinc-900">
          Welcome back, {user?.firstName}
        </h1>
        <p className="mt-1 text-zinc-500">
          Your financial intelligence dashboard is ready. Phase 1 scaffolding is complete.
        </p>
      </div>

      <div className="mb-8 grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total Transactions</CardDescription>
            <CardTitle className="text-3xl">—</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-zinc-500">Available in Phase 2</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Monthly Spending</CardDescription>
            <CardTitle className="text-3xl">—</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-zinc-500">Available in Phase 4</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>AI Insights</CardDescription>
            <CardTitle className="text-3xl">—</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-zinc-500">Available in Phase 5</p>
          </CardContent>
        </Card>
      </div>

      <h2 className="mb-4 text-lg font-semibold text-zinc-900">Coming Next</h2>
      <div className="grid gap-4 sm:grid-cols-2">
        {upcomingFeatures.map(({ icon: Icon, title, phase, description }) => (
          <Card key={title}>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-50">
                    <Icon className="h-4 w-4 text-emerald-600" />
                  </div>
                  <CardTitle className="text-base">{title}</CardTitle>
                </div>
                <span className="rounded-full bg-zinc-100 px-2.5 py-0.5 text-xs font-medium text-zinc-600">
                  {phase}
                </span>
              </div>
              <CardDescription>{description}</CardDescription>
            </CardHeader>
          </Card>
        ))}
      </div>
    </AppShell>
  );
}
