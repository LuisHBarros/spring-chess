import React from 'react';
import { MicroserviceStatus, TelemetryMetric, SqsMessageLog } from '../types';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Activity, Database, Radio, Server, Layers, Cpu, CheckCircle } from 'lucide-react';

interface ObservabilityViewProps {
  services: MicroserviceStatus[];
  metrics: TelemetryMetric[];
  sqsLogs: SqsMessageLog[];
}

export const ObservabilityView: React.FC<ObservabilityViewProps> = ({ services, metrics, sqsLogs }) => {
  return (
    <div className="space-y-6 pb-12">
      {/* Header Banner */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-100">Observability Telemetry Dashboard</h2>
            <Badge variant="default">Observation Stack :9090</Badge>
          </div>
          <p className="text-sm text-slate-400 mt-1">LGTM Stack (Prometheus, Grafana, Tempo S3 Backend, Loki, Promtail).</p>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {metrics.map((m) => (
          <Card key={m.service} className="hover:border-slate-700 transition-all">
            <CardContent className="p-5 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-emerald-400 uppercase tracking-wider font-mono">{m.service}</span>
                <span className="h-2 w-2 rounded-full bg-emerald-400 glow-emerald"></span>
              </div>
              <div className="text-2xl font-extrabold text-slate-100">{m.httpRequestsTotal} <span className="text-xs font-normal text-slate-400">reqs/min</span></div>
              <div className="grid grid-cols-2 gap-2 text-[11px] text-slate-400 pt-2 border-t border-slate-800/80">
                <div>Latency: <span className="text-slate-200 font-mono">{m.avgLatencyMs}ms</span></div>
                <div>SQS Queue: <span className="text-slate-200 font-mono">{m.sqsMessagesProcessed}</span></div>
                <div>SNS Events: <span className="text-slate-200 font-mono">{m.snsEventsPublished}</span></div>
                <div>DB Conns: <span className="text-slate-200 font-mono">{m.activeDbConnections}</span></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* SQS & SNS Event Consumer Stream */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <span>AWS SNS/SQS Message Stream & W3C Trace Propagation</span>
            <Badge variant="outline" className="font-mono text-[10px]">LocalStack Backend</Badge>
          </CardTitle>
          <CardDescription>Live asynchronous domain events published across microservices</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {sqsLogs.map((log) => (
            <div key={log.id} className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800 space-y-2 font-mono text-xs">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Badge variant="default" className="text-[10px]">{log.event}</Badge>
                  <span className="text-slate-400">Queue: {log.queue}</span>
                </div>
                <span className="text-[10px] text-slate-500">{log.timestamp}</span>
              </div>
              <div className="text-[11px] text-emerald-400/90 truncate">
                <span className="text-slate-500">Trace ID:</span> {log.traceId}
              </div>
              <div className="p-2 rounded bg-slate-900 text-slate-300 text-[11px] overflow-x-auto">
                {log.payload}
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
};
