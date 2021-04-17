package chess.domain.dao;

import chess.dao.PieceDAO;
import chess.domain.piece.Color;
import chess.domain.piece.King;
import chess.domain.piece.Pawn;
import chess.domain.piece.Piece;
import chess.dto.PieceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PieceDAOTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private PieceDAO pieceDao;

    @BeforeEach
    public void setup() {
        jdbcTemplate.execute("TRUNCATE TABLE piece");
        jdbcTemplate.execute("TRUNCATE TABLE grid");
        jdbcTemplate.execute("TRUNCATE TABLE room");
        pieceDao = new PieceDAO(jdbcTemplate);
    }

    @Test
    @DisplayName("체스 말을 정상적으로 조회 및 추가를 하는 지 테스트")
    public void createAndFindPieces() {
        long gridId = 1L;
        Piece piece1 = new Pawn(Color.BLACK, 'a', '3');
        Piece piece2 = new King(Color.BLACK, 'a', '4');
        pieceDao.createPiece(gridId, piece1);
        pieceDao.createPiece(gridId, piece2);

        List<PieceDto> pieceDtoGroup = pieceDao.findPiecesByGridId(gridId);
        assertThat(pieceDtoGroup.get(0).toEntity()).isEqualTo(piece1);
        assertThat(pieceDtoGroup.get(1).toEntity()).isEqualTo(piece2);
    }

    @Test
    @DisplayName("체스 말의 정보를 정상적으로 업데이트 하는 지 테스트")
    public void updatePiece() {
        long gridId = 1L;
        Piece piece = new Pawn(Color.BLACK, 'a', '3');
        long pieceId = pieceDao.createPiece(gridId, piece);
        pieceDao.updatePiece(pieceId, false, 'k');

        List<PieceDto> pieceDtoGroup = pieceDao.findPiecesByGridId(gridId);
        PieceDto pieceDto = pieceDtoGroup.get(0);
        assertThat(pieceDto.getIsBlack()).isEqualTo(false);
        assertThat(pieceDto.getName()).isEqualTo("k");
    }
}