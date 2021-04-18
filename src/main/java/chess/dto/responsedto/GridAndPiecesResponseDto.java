package chess.dto.responsedto;

import chess.dto.GridDto;
import chess.dto.PieceDto;

import java.util.ArrayList;
import java.util.List;

public class GridAndPiecesResponseDto {
    private final GridDto gridDto;
    private final List<PieceDto> piecesResponseDto;

    public GridAndPiecesResponseDto(GridDto gridDto, List<PieceDto> piecesResponseDto) {
        this.gridDto = gridDto;
        this.piecesResponseDto = new ArrayList<>(piecesResponseDto);
    }

    public GridDto getGridDto() {
        return gridDto;
    }

    public List<PieceDto> getPiecesResponseDto() {
        return piecesResponseDto;
    }
}
