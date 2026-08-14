import Link from "next/link";
import { ArrowRight, BarChart3, Brain, Upload, Wallet } from "lucide-react";
import { Button } from "@/components/ui/button";

const features = [
  {
    icon: Upload,
    title: "Multi-Source Import",
    description: "Import transactions from CSV exports across multiple bank formats.",
  },
  {
    icon: BarChart3,
    title: "Smart Analytics",
    description: "Visualize spending patterns with interactive charts and trend analysis.",
  },
  {
    icon: Brain,
    title: "AI Insights",
    description: "Get intelligent recommendations powered by AI to optimize your finances.",
  },
];

export default function HomePage() {
  return (
    <div className="min-h-screen">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-600">
              <Wallet className="h-4 w-4 text-white" />
            </div>
            <span className="text-lg font-semibold">Expense Intelligence</span>
          </div>
          <div className="flex items-center gap-3">
            <Link href="/login">
              <Button variant="ghost">Sign in</Button>
            </Link>
            <Link href="/register">
              <Button>Get Started</Button>
            </Link>
          </div>
        </div>
      </header>

      <section className="mx-auto max-w-6xl px-6 py-24 text-center">
        <div className="mx-auto max-w-3xl">
          <p className="mb-4 text-sm font-medium text-emerald-600">
            AI-Powered Financial Intelligence
          </p>
          <h1 className="text-5xl font-bold tracking-tight text-zinc-900">
            Understand your spending.
            <br />
            <span className="text-emerald-600">Make smarter decisions.</span>
          </h1>
          <p className="mt-6 text-lg text-zinc-600">
            Import expenses from multiple sources, analyze spending habits, and receive
            AI-powered insights — all in one platform.
          </p>
          <div className="mt-8 flex items-center justify-center gap-4">
            <Link href="/register">
              <Button size="lg">
                Start for free
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
            <Link href="/login">
              <Button variant="outline" size="lg">
                Sign in
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <section className="border-t border-zinc-200 bg-white py-20">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="text-center text-2xl font-bold text-zinc-900">
            Everything you need for financial clarity
          </h2>
          <div className="mt-12 grid gap-8 md:grid-cols-3">
            {features.map(({ icon: Icon, title, description }) => (
              <div
                key={title}
                className="rounded-xl border border-zinc-200 p-6 text-center"
              >
                <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-lg bg-emerald-50">
                  <Icon className="h-6 w-6 text-emerald-600" />
                </div>
                <h3 className="text-lg font-semibold text-zinc-900">{title}</h3>
                <p className="mt-2 text-sm text-zinc-600">{description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
