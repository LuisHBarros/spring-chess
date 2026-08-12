import React, { useState, useEffect } from 'react';
import { GameState, User, PieceColor, ChessPiece, MoveRecord, ChatMessage } from '../types';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { Clock, RotateCcw, Flag, Handshake, Send, Swords, Award, AlertCircle } from 'lucide-react';
import confetti from 'canvas-confetti';

interface ChessBoardViewProps {
  gameState: GameState;
  currentUser: User;
  onMoveExecuted: (from: string, to: string) => void;
  onResign: (playerColor: PieceColor) => void;
  onResetGame: () => void;
  matchMessages: ChatMessage[];
  onSendMatchMessage: (text: string) => void;
}

export const ChessBoardView: React.FC<ChessBoardViewProps> = ({
  gameState,
  currentUser,
  onMoveExecuted,
  onResign,
  onResetGame,
  matchMessages,
  onSendMatchMessage,
}) => {
  const [selectedSquare, setSelectedSquare] = useState<{ r: number; c: number } | null>(null);
  const [validMoves, setValidMoves] = useState<{ r: number; c: number }[]>([]);
  const [flipped, setFlipped] = useState<boolean>(false);
  const [chatInput, setChatInput] = useState('');
  const [whiteTimer, setWhiteTimer] = useState<number>(gameState.whiteTimeSec);
  const [blackTimer, setBlackTimer] = useState<number>(gameState.blackTimeSec);

  // Unicode chess symbols
  const PIECE_SYMBOLS: Record<string, string> = {
    w_k: '♔', w_q: '♕', w_r: '♖', w_b: '♗', w_n: '♘', w_p: '♙',
    b_k: '♚', b_q: '♛', b_r: '♜', b_b: '♝', b_n: '♞', b_p: '♟',
  };

  // Clock countdown timer effect
  useEffect(() => {
    if (gameState.status !== 'IN_PROGRESS' && gameState.status !== 'CHECK') return;

    const interval = setInterval(() => {
      if (gameState.turn === 'w') {
        setWhiteTimer((prev) => Math.max(0, prev - 1));
      } else {
        setBlackTimer((prev) => Math.max(0, prev - 1));
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [gameState.turn, gameState.status]);

  // Format seconds to mm:ss
  const formatTime = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const getSquareName = (r: number, c: number): string => {
    const file = String.fromCharCode(97 + c); // 'a' - 'h'
    const rank = 8 - r;                       // 8 - 1
    return `${file}${rank}`;
  };

  // Compute legal destination squares for mock chess interaction
  const calculateValidMovesForSquare = (r: number, c: number) => {
    const piece = gameState.board[r][c];
    if (!piece) return [];
    if (piece.color !== gameState.turn) return [];

    const moves: { r: number; c: number }[] = [];
    const dir = piece.color === 'w' ? -1 : 1;

    // Pawn basic forward & captures
    if (piece.type === 'p') {
      if (r + dir >= 0 && r + dir < 8 && !gameState.board[r + dir][c]) {
        moves.push({ r: r + dir, c });
        // double step
        const startRank = piece.color === 'w' ? 6 : 1;
        if (r === startRank && !gameState.board[r + 2 * dir][c]) {
          moves.push({ r: r + 2 * dir, c });
        }
      }
      // Captures
      [-1, 1].forEach((dc) => {
        const nr = r + dir;
        const nc = c + dc;
        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
          const target = gameState.board[nr][nc];
          if (target && target.color !== piece.color) {
            moves.push({ r: nr, c: nc });
          }
        }
      });
    } else {
      // General mobility simulation for Knight, Bishop, Rook, Queen, King
      const deltas = piece.type === 'n'
        ? [[-2, -1], [-2, 1], [-1, -2], [-1, 2], [1, -2], [1, 2], [2, -1], [2, 1]]
        : [[-1, 0], [1, 0], [0, -1], [0, 1], [-1, -1], [-1, 1], [1, -1], [1, 1]];

      deltas.forEach(([dr, dc]) => {
        const nr = r + dr;
        const nc = c + dc;
        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
          const target = gameState.board[nr][nc];
          if (!target || target.color !== piece.color) {
            moves.push({ r: nr, c: nc });
          }
        }
      });
    }

    return moves;
  };

  const handleSquareClick = (r: number, c: number) => {
    if (gameState.status === 'CHECKMATE' || gameState.status === 'RESIGNED') return;

    if (selectedSquare) {
      // Check if clicking a valid move destination
      const isTarget = validMoves.some((m) => m.r === r && m.c === c);
      if (isTarget) {
        const fromSq = getSquareName(selectedSquare.r, selectedSquare.c);
        const toSq = getSquareName(r, c);
        onMoveExecuted(fromSq, toSq);
        setSelectedSquare(null);
        setValidMoves([]);
        return;
      }
    }

    // Select new piece
    const piece = gameState.board[r][c];
    if (piece && piece.color === gameState.turn) {
      setSelectedSquare({ r, c });
      setValidMoves(calculateValidMovesForSquare(r, c));
    } else {
      setSelectedSquare(null);
      setValidMoves([]);
    }
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim()) return;
    onSendMatchMessage(chatInput.trim());
    setChatInput('');
  };

  // Compute captured pieces
  const initialCounts = { p: 8, r: 2, n: 2, b: 2, q: 1 };
  const currentCounts = { w: { p: 0, r: 0, n: 0, b: 0, q: 0 }, b: { p: 0, r: 0, n: 0, b: 0, q: 0 } };

  gameState.board.forEach((row) => {
    row.forEach((cell) => {
      if (cell && cell.type !== 'k') {
        currentCounts[cell.color][cell.type as keyof typeof initialCounts]++;
      }
    });
  });

  const capturedByWhite: string[] = [];
  const capturedByBlack: string[] = [];

  (Object.keys(initialCounts) as (keyof typeof initialCounts)[]).forEach((type) => {
    const missingBlack = initialCounts[type] - currentCounts.b[type];
    for (let i = 0; i < missingBlack; i++) capturedByWhite.push(PIECE_SYMBOLS[`b_${type}`]);

    const missingWhite = initialCounts[type] - currentCounts.w[type];
    for (let i = 0; i < missingWhite; i++) capturedByBlack.push(PIECE_SYMBOLS[`w_${type}`]);
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Top Match Bar Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-xl bg-slate-900 border border-slate-800">
        <div className="flex items-center space-x-3">
          <div className="h-10 w-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Swords className="h-5 w-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-slate-100">Live Match #{gameState.id}</h2>
              <Badge variant="default">{gameState.timeControl}</Badge>
              {gameState.status === 'CHECKMATE' && (
                <Badge variant="destructive">CHECKMATE - Winner: {gameState.winner === 'w' ? 'White' : 'Black'}</Badge>
              )}
            </div>
            <p className="text-xs text-slate-400">Game Engine Microservice (Port :8083) • AWS DynamoDB History</p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center space-x-2">
          <Button variant="outline" size="sm" onClick={() => setFlipped(!flipped)}>
            <RotateCcw className="h-4 w-4 mr-1.5" /> Flip Board
          </Button>
          <Button variant="outline" size="sm" onClick={() => onResign(gameState.turn)}>
            <Flag className="h-4 w-4 mr-1.5 text-red-400" /> Resign
          </Button>
          <Button variant="secondary" size="sm" onClick={onResetGame}>
            Reset Position
          </Button>
        </div>
      </div>

      {/* Main Board & Sidebar Section */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Player Cards & Interactive 8x8 Chessboard */}
        <div className="lg:col-span-8 space-y-3">
          {/* Black Player Card */}
          <div className={`p-3 rounded-xl border flex items-center justify-between transition-all ${
            gameState.turn === 'b' ? 'bg-slate-900 border-emerald-500/50 shadow-md' : 'bg-slate-950/60 border-slate-800'
          }`}>
            <div className="flex items-center space-x-3">
              <img src={gameState.blackPlayer.avatar} alt="" className="h-10 w-10 rounded-lg object-cover ring-1 ring-slate-700" />
              <div>
                <div className="font-semibold text-slate-200 text-sm flex items-center gap-1.5">
                  <span className="h-3 w-3 rounded-full bg-slate-900 border border-slate-500 inline-block"></span>
                  {gameState.blackPlayer.username} ({gameState.blackPlayer.elo})
                  {gameState.blackPlayer.title && <span className="bg-amber-500/20 text-amber-300 text-[10px] px-1 rounded">{gameState.blackPlayer.title}</span>}
                </div>
                <div className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                  Captured: {capturedByBlack.join(' ') || 'None'}
                </div>
              </div>
            </div>

            <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg font-mono text-base font-bold ${
              gameState.turn === 'b' ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30' : 'bg-slate-800 text-slate-400'
            }`}>
              <Clock className="h-4 w-4" />
              {formatTime(blackTimer)}
            </div>
          </div>

          {/* 8x8 Chessboard Grid */}
          <div className="aspect-square max-w-[580px] mx-auto rounded-xl border-4 border-slate-800 bg-slate-900 shadow-2xl overflow-hidden grid grid-cols-8 grid-rows-8 select-none">
            {(flipped ? [7,6,5,4,3,2,1,0] : [0,1,2,3,4,5,6,7]).map((r) =>
              (flipped ? [7,6,5,4,3,2,1,0] : [0,1,2,3,4,5,6,7]).map((c) => {
                const isDark = (r + c) % 2 === 1;
                const piece = gameState.board[r][c];
                const isSelected = selectedSquare?.r === r && selectedSquare?.c === c;
                const isValidTarget = validMoves.some((m) => m.r === r && m.c === c);
                const sqName = getSquareName(r, c);
                const isLastMove = gameState.lastMove?.from === sqName || gameState.lastMove?.to === sqName;

                return (
                  <button
                    key={`${r}-${c}`}
                    onClick={() => handleSquareClick(r, c)}
                    className={`relative flex items-center justify-center text-4xl sm:text-5xl font-bold transition-all duration-100 ${
                      isDark ? 'sq-dark' : 'sq-light'
                    } ${isSelected ? 'sq-selected' : ''} ${isLastMove ? 'sq-last-move' : ''}`}
                  >
                    {/* Rank/File coordinates display */}
                    {c === (flipped ? 7 : 0) && (
                      <span className={`absolute top-0.5 left-1 text-[10px] font-semibold opacity-60 ${isDark ? 'text-slate-300' : 'text-slate-600'}`}>
                        {8 - r}
                      </span>
                    )}
                    {r === (flipped ? 0 : 7) && (
                      <span className={`absolute bottom-0.5 right-1 text-[10px] font-semibold opacity-60 ${isDark ? 'text-slate-300' : 'text-slate-600'}`}>
                        {String.fromCharCode(97 + c)}
                      </span>
                    )}

                    {/* Piece icon */}
                    {piece && (
                      <span
                        className={`transform hover:scale-105 transition-transform ${
                          piece.color === 'w'
                            ? 'text-slate-100 drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)]'
                            : 'text-slate-950 drop-shadow-[0_1px_2px_rgba(255,255,255,0.4)]'
                        }`}
                      >
                        {PIECE_SYMBOLS[`${piece.color}_${piece.type}`]}
                      </span>
                    )}

                    {/* Move hint dots */}
                    {isValidTarget && (
                      <span className={`absolute inset-0 flex items-center justify-center pointer-events-none`}>
                        {piece ? (
                          <span className="w-full h-full border-4 border-red-500/80 rounded-md animate-pulse"></span>
                        ) : (
                          <span className="h-4 w-4 rounded-full bg-emerald-500/80 shadow-lg glow-emerald"></span>
                        )}
                      </span>
                    )}
                  </button>
                );
              })
            )}
          </div>

          {/* White Player Card */}
          <div className={`p-3 rounded-xl border flex items-center justify-between transition-all ${
            gameState.turn === 'w' ? 'bg-slate-900 border-emerald-500/50 shadow-md' : 'bg-slate-950/60 border-slate-800'
          }`}>
            <div className="flex items-center space-x-3">
              <img src={gameState.whitePlayer.avatar} alt="" className="h-10 w-10 rounded-lg object-cover ring-1 ring-slate-700" />
              <div>
                <div className="font-semibold text-slate-200 text-sm flex items-center gap-1.5">
                  <span className="h-3 w-3 rounded-full bg-slate-100 inline-block"></span>
                  {gameState.whitePlayer.username} ({gameState.whitePlayer.elo})
                  {gameState.whitePlayer.title && <span className="bg-amber-500/20 text-amber-300 text-[10px] px-1 rounded">{gameState.whitePlayer.title}</span>}
                </div>
                <div className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                  Captured: {capturedByWhite.join(' ') || 'None'}
                </div>
              </div>
            </div>

            <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg font-mono text-base font-bold ${
              gameState.turn === 'w' ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30' : 'bg-slate-800 text-slate-400'
            }`}>
              <Clock className="h-4 w-4" />
              {formatTime(whiteTimer)}
            </div>
          </div>
        </div>

        {/* Right Column: Move History SAN & Integrated Match Chat */}
        <div className="lg:col-span-4 space-y-4">
          {/* SAN Move History Log */}
          <Card>
            <CardHeader className="py-3 px-4 flex flex-row items-center justify-between">
              <CardTitle className="text-sm">Move History (DynamoDB)</CardTitle>
              <Badge variant="outline" className="font-mono text-[10px]">{gameState.moveHistory.length} moves</Badge>
            </CardHeader>
            <CardContent className="p-0">
              <div className="h-48 overflow-y-auto p-3 font-mono text-xs space-y-1 divide-y divide-slate-800/50">
                {gameState.moveHistory.map((m, idx) => {
                  if (m.player === 'w') {
                    const blackMove = gameState.moveHistory[idx + 1];
                    return (
                      <div key={m.moveNumber} className="grid grid-cols-12 py-1 items-center text-slate-300">
                        <span className="col-span-3 text-slate-500 font-bold">{m.moveNumber}.</span>
                        <span className="col-span-4 text-emerald-400 font-semibold">{m.san}</span>
                        <span className="col-span-5 text-slate-400">{blackMove ? blackMove.san : ''}</span>
                      </div>
                    );
                  }
                  return null;
                })}
              </div>
            </CardContent>
          </Card>

          {/* Real-time Match Chat */}
          <Card className="flex flex-col h-[320px]">
            <CardHeader className="py-3 px-4">
              <CardTitle className="text-sm">Match Chat (Chat Service)</CardTitle>
            </CardHeader>
            <CardContent className="p-3 flex-1 flex flex-col justify-between overflow-hidden">
              <div className="overflow-y-auto space-y-2 pr-1 flex-1">
                {matchMessages.map((msg) => (
                  <div key={msg.id} className="text-xs">
                    {msg.isSystem ? (
                      <div className="text-center text-[10px] text-amber-400/80 bg-amber-500/10 py-1 rounded border border-amber-500/20">
                        {msg.text}
                      </div>
                    ) : (
                      <div className="space-y-0.5">
                        <span className="font-semibold text-slate-400 mr-1.5">{msg.senderName}:</span>
                        <span className="text-slate-200">{msg.text}</span>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              <form onSubmit={handleSendMessage} className="flex gap-2 pt-2 border-t border-slate-800">
                <input
                  type="text"
                  placeholder="Send in-game message..."
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-emerald-500"
                />
                <Button type="submit" variant="emerald" size="sm" className="h-8 w-8 p-0">
                  <Send className="h-3.5 w-3.5" />
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
