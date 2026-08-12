export type ViewType = 'dashboard' | 'lobby' | 'game' | 'social' | 'chat' | 'observability';

export interface User {
  id: string;
  username: string;
  email: string;
  title?: string;
  elo: number;
  avatar: string;
  status: 'online' | 'in_game' | 'offline';
  blitzElo: number;
  rapidElo: number;
  bulletElo: number;
  wins: number;
  losses: number;
  draws: number;
}

export interface MicroserviceStatus {
  name: string;
  serviceKey: 'auth' | 'social' | 'chat' | 'game' | 'observation';
  port: number;
  status: 'HEALTHY' | 'DEGRADED' | 'DOWN';
  latencyMs: number;
  tech: string;
  dbType: string;
}

export type PieceType = 'p' | 'r' | 'n' | 'b' | 'q' | 'k';
export type PieceColor = 'w' | 'b';

export interface ChessPiece {
  type: PieceType;
  color: PieceColor;
}

export interface MoveRecord {
  moveNumber: number;
  player: PieceColor;
  from: string; // e.g. "e2"
  to: string;   // e.g. "e4"
  piece: PieceType;
  captured?: PieceType;
  san: string;  // e.g. "e4"
  timestamp: string;
}

export interface GameState {
  id: string;
  whitePlayer: User;
  blackPlayer: User;
  turn: PieceColor;
  status: 'PENDING' | 'IN_PROGRESS' | 'CHECK' | 'CHECKMATE' | 'STALEMATE' | 'RESIGNED' | 'DRAW';
  winner: PieceColor | 'draw' | null;
  board: (ChessPiece | null)[][]; // 8x8 matrix
  moveHistory: MoveRecord[];
  whiteTimeSec: number;
  blackTimeSec: number;
  lastMove?: { from: string; to: string };
  timeControl: string;
}

export interface Friend {
  id: string;
  username: string;
  avatar: string;
  title?: string;
  elo: number;
  status: 'online' | 'in_game' | 'offline';
  currentActivity?: string;
}

export interface Guild {
  id: string;
  name: string;
  tag: string;
  membersCount: number;
  leader: string;
  level: number;
  rank: number;
  description: string;
  banner: string;
}

export interface ChatMessage {
  id: string;
  channelId: string;
  senderId: string;
  senderName: string;
  senderAvatar?: string;
  text: string;
  timestamp: string;
  isSystem?: boolean;
}

export interface TelemetryMetric {
  service: string;
  httpRequestsTotal: number;
  avgLatencyMs: number;
  sqsMessagesProcessed: number;
  snsEventsPublished: number;
  activeDbConnections: number;
}

export interface SqsMessageLog {
  id: string;
  event: string;
  queue: string;
  traceId: string;
  payload: string;
  timestamp: string;
}
