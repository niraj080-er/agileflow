package com.swiggy.agileflow.project.api;

import com.swiggy.agileflow.project.application.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Board", description = "Project board with issues grouped by workflow status.")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/{id}/board")
    @Operation(summary = "Get board state",
        description = "Returns workflow status columns with issues grouped by status. "
            + "Optional sprintId filter restricts issues to a specific sprint. "
            + "Returns 404 for missing project or sprint, 422 if sprint belongs to another project.")
    public BoardResponse getBoard(
            @PathVariable Long id,
            @RequestParam(required = false) Long sprintId) {
        return boardService.getBoard(id, sprintId);
    }
}
