package br.com.dio.model;

import java.util.Collection;
import java.util.HashSet; // Adicionado para usar Set para verificações de unicidade
import java.util.List;
import java.util.Set;    // Adicionado para usar Set para verificações de unicidade

import static br.com.dio.model.GameStatusEnum.COMPLETE; //
import static br.com.dio.model.GameStatusEnum.INCOMPLETE; //
import static br.com.dio.model.GameStatusEnum.NON_STARTED; //
import static java.util.Objects.isNull; //
import static java.util.Objects.nonNull; //

public class Board {

    private final List<List<Space>> spaces; //

    public Board(final List<List<Space>> spaces) { //
        this.spaces = spaces; //
    }

    public List<List<Space>> getSpaces() { //
        return spaces; //
    }

    public GameStatusEnum getStatus(){ //
        if (spaces.stream().flatMap(Collection::stream).noneMatch(s -> !s.isFixed() && nonNull(s.getActual()))){ //
            return NON_STARTED; //
        }

        return spaces.stream().flatMap(Collection::stream).anyMatch(s -> isNull(s.getActual())) ? INCOMPLETE : COMPLETE; //
    }

    // -- INÍCIO DAS MELHORIAS DE VALIDAÇÃO DE SUDOKU --

    /**
     * Verifica se um movimento (inserir um valor em uma posição) é válido de acordo com as regras do Sudoku.
     * Ou seja, o valor não pode estar duplicado na linha, coluna ou bloco 3x3.
     * IMPORTANTE: Esta validação é feita *sem* alterar o tabuleiro.
     *
     * @param col Coluna (0-8)
     * @param row Linha (0-8)
     * @param value Valor a ser inserido (1-9)
     * @return true se o movimento for válido, false caso contrário.
     */
    public boolean isValidMove(final int col, final int row, final int value) {
        // Verifica a linha
        for (int c = 0; c < 9; c++) {
            if (c == col) continue; // Pula a própria célula que está sendo verificada
            if (nonNull(spaces.get(row).get(c).getActual()) && spaces.get(row).get(c).getActual().equals(value)) {
                return false; // Valor já existe na linha
            }
        }

        // Verifica a coluna
        for (int r = 0; r < 9; r++) {
            if (r == row) continue; // Pula a própria célula que está sendo verificada
            if (nonNull(spaces.get(r).get(col).getActual()) && spaces.get(r).get(col).getActual().equals(value)) {
                return false; // Valor já existe na coluna
            }
        }

        // Verifica o bloco 3x3
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (startRow + r == row && startCol + c == col) continue; // Pula a própria célula
                if (nonNull(spaces.get(startRow + r).get(startCol + c).getActual()) &&
                    spaces.get(startRow + r).get(startCol + c).getActual().equals(value)) {
                    return false; // Valor já existe no bloco 3x3
                }
            }
        }

        return true; // O movimento é válido
    }


    /**
     * Verifica se o tabuleiro contém erros, tanto em relação à solução esperada
     * quanto em relação às regras do Sudoku (duplicatas em linha, coluna, bloco).
     *
     * @return true se houver erros, false caso contrário.
     */
    @Override
    public boolean hasErrors(){ //
        // Primeiro, verifica se há valores atuais diferentes dos esperados (erros de solução)
        boolean solutionErrors = spaces.stream().flatMap(Collection::stream) //
                .anyMatch(s -> nonNull(s.getActual()) && !s.getActual().equals(s.getExpected())); //

        if (solutionErrors) {
            return true;
        }

        // Segundo, verifica erros de regra do Sudoku (duplicatas na linha, coluna, bloco)
        for (int i = 0; i < 9; i++) {
            if (hasDuplicateInRow(i) || hasDuplicateInColumn(i)) {
                return true;
            }
        }

        for (int rowBlock = 0; rowBlock < 3; rowBlock++) {
            for (int colBlock = 0; colBlock < 3; colBlock++) {
                if (hasDuplicateInBlock(rowBlock * 3, colBlock * 3)) {
                    return true;
                }
            }
        }

        return false; // Nenhum erro encontrado
    }

    private boolean hasDuplicateInRow(int row) {
        Set<Integer> seen = new HashSet<>();
        for (int col = 0; col < 9; col++) {
            Integer value = spaces.get(row).get(col).getActual();
            if (nonNull(value)) {
                if (seen.contains(value)) {
                    return true; // Duplicata encontrada na linha
                }
                seen.add(value);
            }
        }
        return false;
    }

    private boolean hasDuplicateInColumn(int col) {
        Set<Integer> seen = new HashSet<>();
        for (int row = 0; row < 9; row++) {
            Integer value = spaces.get(row).get(col).getActual();
            if (nonNull(value)) {
                if (seen.contains(value)) {
                    return true; // Duplicata encontrada na coluna
                }
                seen.add(value);
            }
        }
        return false;
    }

    private boolean hasDuplicateInBlock(int startRow, int startCol) {
        Set<Integer> seen = new HashSet<>();
        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                Integer value = spaces.get(row).get(col).getActual();
                if (nonNull(value)) {
                    if (seen.contains(value)) {
                        return true; // Duplicata encontrada no bloco
                    }
                    seen.add(value);
                }
            }
        }
        return false;
    }

    @Override
    public boolean changeValue(final int col, final int row, final int value){ //
        var space = spaces.get(row).get(col); // Acessa por row, depois col
        if (space.isFixed()){ //
            return false; //
        }

        // Adiciona a validação das regras do Sudoku AQUI!
        // Antes de definir o valor, verifica se ele é válido de acordo com as regras.
        // Temporariamente define o valor para testar, ou passa o valor para isValidMove
        // Para simplificar, isValidMove já considera o valor que *seria* colocado.
        
        // Verifica se o valor é válido para ser colocado na posição
        // Se a posição já tem um valor e ele está sendo substituído, isValidMove precisa considerar isso.
        // A forma como isValidMove está escrita já lida com isso porque ele ignora a própria célula.
        if (!isValidMove(col, row, value)) { // Usando o novo método de validação
            System.out.println("Movimento inválido! O número já existe na linha, coluna ou bloco 3x3.");
            return false;
        }

        space.setActual(value); //
        return true; //
    }

    @Override
    public boolean clearValue(final int col, final int row){ //
        var space = spaces.get(row).get(col); // Acessa por row, depois col
        if (space.isFixed()){ //
            return false; //
        }

        space.clearSpace(); //
        return true; //
    }

    @Override
    public void reset(){ //
        spaces.forEach(c -> c.forEach(Space::clearSpace)); //
    }

    @Override
    public boolean gameIsFinished(){ //
        // O jogo só está finalizado se estiver COMPLETO e NÃO TIVER ERROS de regra OU de solução
        return getStatus().equals(COMPLETE) && !hasErrors(); //
    }

    // -- FIM DAS MELHORIAS DE VALIDAÇÃO DE SUDOKU --

}
