import React, { useState } from 'react';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';
import { DashboardView } from './views/DashboardView';
import { ChessBoardView } from './views/ChessBoardView';
import { LobbyView } from './views/LobbyView';
import { SocialGuildsView } from './views/SocialGuildsView';
import { ChatView } from './views/ChatView';
import { ObservabilityView } from './views/ObservabilityView';
import {
  MOCK_USERS,
  MOCK_SERVICES,
  INITIAL_GAME_STATE,
  MOCK_FRIENDS,
  MOCK_GUILDS,
  MOCK_CHAT_MESSAGES,
  MOCK_TELEMETRY,
  MOCK_SQS_LOGS,
  getInitialBoard,
} from './mock/initialState';
import { ViewType, User, GameState, PieceColor, ChatMessage, SqsMessageLog } from './types';
import confetti from 'canvas-confetti';

export function App() {
  const [currentUser, setCurrentUser] = useState<User>(MOCK_USERS[0]);
  const [activeView, setActiveView] = useState<ViewType>('dashboard');
  const [gameState, setGameState] = useState<GameState>(INITIAL_GAME_STATE);
  const [messages, setMessages] = useState<ChatMessage[]>(MOCK_CHAT_MESSAGES);
  const [sqsLogs, setSqsLogs] = useState<SqsMessageLog[]>(MOCK_SQS_LOGS);

  // Handle Chess move executed
  const handleMoveExecuted = (from: string, to: string) => {
    // Helper to translate algebraic notation (e.g. 'e4') to r,c
    const parseSquare = (sq: string) => {
      const file = sq.charCodeAt(0) - 97;
      const rank = 8 - parseInt(sq[1], 10);
      return { r: rank, c: file };
    };

    const fromCoord = parseSquare(from);
    const toCoord = parseSquare(to);

    const newBoard = gameState.board.map((row) => [...row]);
    const piece = newBoard[fromCoord.r][fromCoord.c];
    if (!piece) return;

    const captured = newBoard[toCoord.r][toCoord.c];
    newBoard[toCoord.r][toCoord.c] = piece;
    newBoard[fromCoord.r][fromCoord.c] = null;

    const sanNotation = `${piece.type !== 'p' ? piece.type.toUpperCase() : ''}${captured ? 'x' : ''}${to}`;
    const nextTurn: PieceColor = gameState.turn === 'w' ? 'b' : 'w';

    const newMoveRecord = {
      moveNumber: Math.floor(gameState.moveHistory.length / 2) + 1,
      player: gameState.turn,
      from,
      to,
      piece: piece.type,
      captured: captured ? captured.type : undefined,
      san: sanNotation,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    };

    setGameState((prev) => ({
      ...prev,
      board: newBoard,
      turn: nextTurn,
      lastMove: { from, to },
      moveHistory: [...prev.moveHistory, newMoveRecord],
    }));

    // Log SQS Event in Telemetry
    const newLog: SqsMessageLog = {
      id: `msg_${Date.now()}`,
      event: 'MoveExecutedEvent',
      queue: 'game-history-topic',
      traceId: `00-${Math.random().toString(16).substring(2, 18)}-${Math.random().toString(16).substring(2, 18)}-01`,
      payload: JSON.stringify({ gameId: gameState.id, move: sanNotation, turn: nextTurn }),
      timestamp: new Date().toLocaleTimeString(),
    };
    setSqsLogs((prev) => [newLog, ...prev]);
  };

  const handleResign = (playerColor: PieceColor) => {
    const winnerColor = playerColor === 'w' ? 'b' : 'w';
    setGameState((prev) => ({
      ...prev,
      status: 'RESIGNED',
      winner: winnerColor,
    }));
    confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });
  };

  const handleResetGame = () => {
    setGameState({
      ...INITIAL_GAME_STATE,
      board: getInitialBoard(),
      moveHistory: [],
      status: 'IN_PROGRESS',
      turn: 'w',
    });
  };

  const handleStartMatch = (timeControl: string) => {
    handleResetGame();
    setGameState((prev) => ({ ...prev, timeControl }));
    setActiveView('game');
  };

  const handleSendMessage = (channelId: string, text: string) => {
    const newMsg: ChatMessage = {
      id: `m_${Date.now()}`,
      channelId,
      senderId: currentUser.id,
      senderName: currentUser.username,
      senderAvatar: currentUser.avatar,
      text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };
    setMessages((prev) => [...prev, newMsg]);
  };

  return (
    <div className="min-h-screen bg-[#0b0f19] text-slate-100 flex flex-col antialiased">
      <Navbar
        currentUser={currentUser}
        allUsers={MOCK_USERS}
        onSelectUser={setCurrentUser}
        services={MOCK_SERVICES}
        activeView={activeView}
        onNavigate={setActiveView}
      />

      <div className="flex flex-1 max-w-7xl w-full mx-auto">
        <Sidebar activeView={activeView} onNavigate={setActiveView} />

        <main className="flex-1 p-4 md:p-6 overflow-y-auto">
          {activeView === 'dashboard' && (
            <DashboardView
              currentUser={currentUser}
              services={MOCK_SERVICES}
              friends={MOCK_FRIENDS}
              onNavigate={setActiveView}
            />
          )}

          {activeView === 'lobby' && (
            <LobbyView
              currentUser={currentUser}
              onStartMatch={handleStartMatch}
              onNavigate={setActiveView}
            />
          )}

          {activeView === 'game' && (
            <ChessBoardView
              gameState={gameState}
              currentUser={currentUser}
              onMoveExecuted={handleMoveExecuted}
              onResign={handleResign}
              onResetGame={handleResetGame}
              matchMessages={messages.filter((m) => m.channelId === 'match')}
              onSendMatchMessage={(text) => handleSendMessage('match', text)}
            />
          )}

          {activeView === 'social' && (
            <SocialGuildsView
              currentUser={currentUser}
              friends={MOCK_FRIENDS}
              guilds={MOCK_GUILDS}
              onNavigate={setActiveView}
              onChallengeFriend={(friendName) => handleStartMatch('5+0 Rapid')}
            />
          )}

          {activeView === 'chat' && (
            <ChatView
              currentUser={currentUser}
              messages={messages}
              onSendMessage={handleSendMessage}
            />
          )}

          {activeView === 'observability' && (
            <ObservabilityView
              services={MOCK_SERVICES}
              metrics={MOCK_TELEMETRY}
              sqsLogs={sqsLogs}
            />
          )}
        </main>
      </div>
    </div>
  );
}

export default App;
