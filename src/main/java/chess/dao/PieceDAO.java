package chess.dao;

import chess.domain.piece.Piece;
import chess.dto.PieceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PieceDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public long createPiece(long gridId, Piece piece) {
        boolean isBlack = piece.isBlack();
        String position = String.valueOf(piece.position().x()) + String.valueOf(piece.position().y());
        String name = String.valueOf(piece.name());
        String query = "INSERT INTO piece (isBlack, position, gridId, name) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setBoolean(1, isBlack);
            ps.setString(2, position);
            ps.setLong(3, gridId);
            ps.setString(4, name);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<PieceDto> findPiecesByGridId(long gridId) {
        String query = "SELECT * FROM piece WHERE gridId = ?";
        return jdbcTemplate.queryForObject(
                query,
                (rs, rowNum) -> {
                    List<PieceDto> pieces = new ArrayList<>();
                    do {
                        pieces.add(new PieceDto(
                                rs.getLong(1),
                                rs.getBoolean(2),
                                rs.getString(3),
                                rs.getLong(4),
                                rs.getString(5)
                        ));
                    } while(rs.next());
                    return pieces;
                },
                gridId);
    }

    public void updatePiece(long pieceId, boolean isBlack, char name) {
        String query = "UPDATE piece SET isBlack = ?, name = ?  WHERE pieceId = ?";
        jdbcTemplate.update(query, isBlack, String.valueOf(name), pieceId);
    }
}
