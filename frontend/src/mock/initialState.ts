import { User, MicroserviceStatus, ChessPiece, GameState, Friend, Guild, ChatMessage, TelemetryMetric, SqsMessageLog } from '../types';

export const MOCK_USERS: User[] = [
  {
    id: 'usr_01',
    username: 'Alice_GM',
    email: 'alice@chess-platform.io',
    title: 'GM',
    elo: 2640,
    blitzElo: 2680,
    rapidElo: 2610,
    bulletElo: 2710,
    wins: 342,
    losses: 89,
    draws: 64,
    status: 'online',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
  },
  {
    id: 'usr_02',
    username: 'Bob_The_King',
    email: 'bob@chess-platform.io',
    title: 'IM',
    elo: 2410,
    blitzElo: 2390,
    rapidElo: 2430,
    bulletElo: 2450,
    wins: 215,
    losses: 110,
    draws: 45,
    status: 'in_game',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
  },
  {
    id: 'usr_03',
    username: 'Charlie_Knight',
    email: 'charlie@chess-platform.io',
    elo: 1850,
    blitzElo: 1820,
    rapidElo: 1890,
    bulletElo: 1790,
    wins: 95,
    losses: 82,
    draws: 21,
    status: 'online',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80',
  },
];

export const MOCK_SERVICES: MicroserviceStatus[] = [
  {
    name: 'Authentication Microservice',
    serviceKey: 'auth',
    port: 8081,
    status: 'HEALTHY',
    latencyMs: 12,
    tech: 'Spring Boot 3 + PostgreSQL',
    dbType: 'PostgreSQL + LocalStack SQS/SNS',
  },
  {
    name: 'Social Microservice',
    serviceKey: 'social',
    port: 8082,
    status: 'HEALTHY',
    latencyMs: 18,
    tech: 'Spring Boot 3 + Guild Domain',
    dbType: 'PostgreSQL + LocalStack RDS',
  },
  {
    name: 'Game Microservice',
    serviceKey: 'game',
    port: 8083,
    status: 'HEALTHY',
    latencyMs: 8,
    tech: 'Spring Boot 3 + DDD Game Engine',
    dbType: 'PostgreSQL (Active) + DynamoDB (History)',
  },
  {
    name: 'Chat Microservice',
    serviceKey: 'chat',
    port: 8084,
    status: 'HEALTHY',
    latencyMs: 15,
    tech: 'Spring Boot 3 + Real-Time WebSocket',
    dbType: 'Redis + PostgreSQL',
  },
  {
    name: 'Observability Telemetry',
    serviceKey: 'observation',
    port: 9090,
    status: 'HEALTHY',
    latencyMs: 5,
    tech: 'LGTM Stack (Prometheus/Grafana/Loki)',
    dbType: 'Tempo + Promtail',
  },
];

export function getInitialBoard(): (ChessPiece | null)[][] {
  const board: (ChessPiece | null)[][] = Array(8).fill(null).map(() => Array(8).fill(null));

  // Black major pieces (Rank 0)
  board[0] = [
    { type: 'r', color: 'b' },
    { type: 'n', color: 'b' },
    { type: 'b', color: 'b' },
    { type: 'q', color: 'b' },
    { type: 'k', color: 'b' },
    { type: 'b', color: 'b' },
    { type: 'n', color: 'b' },
    { type: 'r', color: 'b' },
  ];
  // Black pawns (Rank 1)
  board[1] = Array(8).fill(null).map(() => ({ type: 'p', color: 'b' }));

  // White pawns (Rank 6)
  board[6] = Array(8).fill(null).map(() => ({ type: 'p', color: 'w' }));
  // White major pieces (Rank 7)
  board[7] = [
    { type: 'r', color: 'w' },
    { type: 'n', color: 'w' },
    { type: 'b', color: 'w' },
    { type: 'q', color: 'w' },
    { type: 'k', color: 'w' },
    { type: 'b', color: 'w' },
    { type: 'n', color: 'w' },
    { type: 'r', color: 'w' },
  ];

  return board;
}

export const INITIAL_GAME_STATE: GameState = {
  id: 'game_984321',
  whitePlayer: MOCK_USERS[0],
  blackPlayer: MOCK_USERS[1],
  turn: 'w',
  status: 'IN_PROGRESS',
  winner: null,
  board: getInitialBoard(),
  moveHistory: [
    { moveNumber: 1, player: 'w', from: 'e2', to: 'e4', piece: 'p', san: 'e4', timestamp: '19:50:12' },
    { moveNumber: 1, player: 'b', from: 'e7', to: 'e5', piece: 'p', san: 'e5', timestamp: '19:50:18' },
    { moveNumber: 2, player: 'w', from: 'g1', to: 'f3', piece: 'n', san: 'Nf3', timestamp: '19:50:24' },
    { moveNumber: 2, player: 'b', from: 'b8', to: 'c6', piece: 'n', san: 'Nc6', timestamp: '19:50:31' },
    { moveNumber: 3, player: 'w', from: 'f1', to: 'b5', piece: 'b', san: 'Bb5', timestamp: '19:50:40' },
  ],
  whiteTimeSec: 285,
  blackTimeSec: 291,
  lastMove: { from: 'f1', to: 'b5' },
  timeControl: '5+0 Rapid',
};

// Slightly update board to match history (e4 e5 Nf3 Nc6 Bb5)
(function applyInitialMoves() {
  const b = INITIAL_GAME_STATE.board;
  // 1. e4 e5
  b[6][4] = null; b[4][4] = { type: 'p', color: 'w' };
  b[1][4] = null; b[3][4] = { type: 'p', color: 'b' };
  // 2. Nf3 Nc6
  b[7][6] = null; b[5][5] = { type: 'n', color: 'w' };
  b[0][1] = null; b[2][2] = { type: 'n', color: 'b' };
  // 3. Bb5
  b[7][5] = null; b[3][1] = { type: 'b', color: 'w' };
})();

export const MOCK_FRIENDS: Friend[] = [
  {
    id: 'usr_02',
    username: 'Bob_The_King',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    title: 'IM',
    elo: 2410,
    status: 'in_game',
    currentActivity: 'Playing vs Alice_GM (Ruy Lopez)',
  },
  {
    id: 'usr_03',
    username: 'Charlie_Knight',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80',
    elo: 1850,
    status: 'online',
    currentActivity: 'In Lobby (Looking for 5+0 Rapid)',
  },
  {
    id: 'usr_04',
    username: 'Diana_Queen',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    title: 'WGM',
    elo: 2310,
    status: 'offline',
    currentActivity: 'Last seen 2 hours ago',
  },
];

export const MOCK_GUILDS: Guild[] = [
  {
    id: 'gld_01',
    name: 'Grandmaster Elite',
    tag: 'GME',
    membersCount: 48,
    leader: 'Alice_GM',
    level: 15,
    rank: 1,
    description: 'Premier competitive chess guild. Weekly tournaments and master post-game analysis.',
    banner: 'https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=600&auto=format&fit=crop&q=80',
  },
  {
    id: 'gld_02',
    name: 'Tactical Knights',
    tag: 'KNIGHTS',
    membersCount: 124,
    leader: 'Charlie_Knight',
    level: 9,
    rank: 4,
    description: 'Friendly community focused on rapid tactical development and blitz ladder climbing.',
    banner: 'https://images.unsplash.com/photo-1560174038-da43ac74f01b?w=600&auto=format&fit=crop&q=80',
  },
];

export const MOCK_CHAT_MESSAGES: ChatMessage[] = [
  {
    id: 'm1',
    channelId: 'global',
    senderId: 'usr_03',
    senderName: 'Charlie_Knight',
    senderAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80',
    text: 'Good luck in the upcoming tournament everyone! 🏆',
    timestamp: '19:42',
  },
  {
    id: 'm2',
    channelId: 'global',
    senderId: 'sys',
    senderName: 'System',
    text: 'User Alice_GM created a new 5+0 Rapid room.',
    timestamp: '19:45',
    isSystem: true,
  },
  {
    id: 'm3',
    channelId: 'match',
    senderId: 'usr_02',
    senderName: 'Bob_The_King',
    senderAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    text: 'A sharp Ruy Lopez line today!',
    timestamp: '19:51',
  },
];

export const MOCK_TELEMETRY: TelemetryMetric[] = [
  { service: 'auth-service', httpRequestsTotal: 1420, avgLatencyMs: 12, sqsMessagesProcessed: 42, snsEventsPublished: 19, activeDbConnections: 5 },
  { service: 'social-service', httpRequestsTotal: 980, avgLatencyMs: 18, sqsMessagesProcessed: 88, snsEventsPublished: 34, activeDbConnections: 8 },
  { service: 'game-service', httpRequestsTotal: 4890, avgLatencyMs: 8, sqsMessagesProcessed: 312, snsEventsPublished: 215, activeDbConnections: 14 },
  { service: 'chat-service', httpRequestsTotal: 3100, avgLatencyMs: 15, sqsMessagesProcessed: 190, snsEventsPublished: 140, activeDbConnections: 12 },
];

export const MOCK_SQS_LOGS: SqsMessageLog[] = [
  {
    id: 'msg_8901',
    event: 'MatchStartedEvent',
    queue: 'game-match-events.fifo',
    traceId: '00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01',
    payload: '{"gameId":"game_984321","white":"usr_01","black":"usr_02","timeControl":"5+0"}',
    timestamp: '19:50:12',
  },
  {
    id: 'msg_8902',
    event: 'MoveExecutedEvent',
    queue: 'game-history-topic',
    traceId: '00-9a4f128c11e74151a66c89100a0b12ff-11c088bb00aa45cc-01',
    payload: '{"gameId":"game_984321","move":"Bb5","moveNumber":3,"turn":"w"}',
    timestamp: '19:50:40',
  },
  {
    id: 'msg_8903',
    event: 'UserPresenceChanged',
    queue: 'social-events-queue',
    traceId: '00-77a012903bb11234c909a8176512bbcd-22a099aa11bb22cc-01',
    payload: '{"userId":"usr_02","status":"IN_GAME","activity":"Playing vs Alice_GM"}',
    timestamp: '19:50:45',
  },
];
