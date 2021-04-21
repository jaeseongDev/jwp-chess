package chess.service;

import chess.dao.GridDAO;
import chess.dao.PieceDAO;
import chess.dao.RoomDAO;
import chess.domain.grid.Grid;
import chess.domain.grid.Line;
import chess.domain.grid.Lines;
import chess.domain.grid.gridStrategy.CustomGridStrategy;
import chess.domain.grid.gridStrategy.NormalGridStrategy;
import chess.domain.piece.Color;
import chess.domain.piece.Piece;
import chess.domain.piece.PieceFactory;
import chess.dto.GridDto;
import chess.dto.PieceDto;
import chess.dto.requestdto.MoveRequestDto;
import chess.dto.requestdto.StartRequestDto;
import chess.dto.response.Response;
import chess.dto.response.ResponseCode;
import chess.dto.responsedto.GridAndPiecesResponseDto;
import chess.dto.responsedto.RoomsResponseDto;
import chess.exception.ChessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChessService {
    private static final char EMPTY_PIECE_NAME = '.';

    private final RoomDAO roomDAO;
    private final GridDAO gridDAO;
    private final PieceDAO pieceDAO;

    public ChessService(RoomDAO roomDAO, GridDAO gridDAO, PieceDAO pieceDAO) {
        this.roomDAO = roomDAO;
        this.gridDAO = gridDAO;
        this.pieceDAO = pieceDAO;
    }

    public Response<MoveRequestDto> move(MoveRequestDto requestDto) {
        GridDto gridDto = requestDto.getGridDto();
        List<Piece> pieces = requestDto.getPiecesDto().stream()
                .map(pieceDto -> {
                    Color color = null;
                    if (pieceDto.getIsBlack()) {
                        color = Color.BLACK;
                    }
                    if (!pieceDto.getIsBlack()) {
                        color = Color.WHITE;
                    }
                    return PieceFactory.from(
                            pieceDto.getName().charAt(0),
                            color, pieceDto.getPosition().charAt(0),
                            pieceDto.getPosition().charAt(1)
                    );
                })
                .collect(Collectors.toList());
        List<Line> lines = Lines.from(pieces).lines();

        Grid grid = new Grid(new CustomGridStrategy(lines, Color.findColorByTurn(requestDto.getGridDto().getIsBlackTurn())));
        grid.move(requestDto.getSourcePosition(), requestDto.getTargetPosition());
        gridDAO.changeTurn(gridDto.getGridId(), !gridDto.getIsBlackTurn());
        PieceDto sourcePieceDto = requestDto.getPiecesDto().stream()
                .filter(pieceDto -> {
                    return pieceDto.getPosition().equals(requestDto.getSourcePosition());
                })
                .findFirst()
                .orElseThrow(() -> new ChessException(ResponseCode.NOT_EXISTING_PIECE));
        PieceDto targetPieceDto = requestDto.getPiecesDto().stream()
                .filter(pieceDto -> {
                    return pieceDto.getPosition().equals(requestDto.getTargetPosition());
                })
                .findFirst()
                .orElseThrow(() -> new ChessException(ResponseCode.NOT_EXISTING_PIECE));
        pieceDAO.updatePiece(sourcePieceDto.getPieceId(), sourcePieceDto.getIsBlack(), EMPTY_PIECE_NAME);
        pieceDAO.updatePiece(targetPieceDto.getPieceId(), sourcePieceDto.getIsBlack(), sourcePieceDto.getName().charAt(0));
        return new Response(HttpStatus.NO_CONTENT);
    }

    public void start(long gridId) throws SQLException {
        gridDAO.changeToStarting(gridId);
    }

    public GridAndPiecesResponseDto getGridAndPieces(StartRequestDto requestDto) {
        String roomName = requestDto.getRoomName();
        Optional<Long> roomId = roomDAO.findRoomIdByName(roomName);
        if (!roomId.isPresent()) {
            long createdRoomId = roomDAO.createRoom(roomName);
            return createGridAndPiece(createdRoomId);
        }
        GridDto gridDto = gridDAO.findRecentGridByRoomId(roomId.get());
        List<PieceDto> piecesResponseDto = pieceDAO.findPiecesByGridId(gridDto.getGridId());
        return new GridAndPiecesResponseDto(gridDto, piecesResponseDto);
    }

    public void finish(long gridId) {
        gridDAO.changeToFinished(gridId);
    }

    public GridAndPiecesResponseDto restart(long roomId) {
        return createGridAndPiece(roomId);
    }

    private GridAndPiecesResponseDto createGridAndPiece(long roomId) {
        Grid grid = new Grid(new NormalGridStrategy());
        List<Piece> pieces = grid.pieces();
        long gridId = gridDAO.createGrid(roomId);
        for (Piece piece : pieces) {
            pieceDAO.createPiece(gridId, piece);
        }
        GridDto gridDto = gridDAO.findGridByGridId(gridId);
        List<PieceDto> piecesResponseDto = pieceDAO.findPiecesByGridId(gridId);
        return new GridAndPiecesResponseDto(gridDto, piecesResponseDto);
    }

    public RoomsResponseDto getAllRooms() {
        return new RoomsResponseDto(roomDAO.findAllRooms());
    }
}
