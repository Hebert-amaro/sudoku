package br.com.dio;

import br.com.dio.model.Board; //
import br.com.dio.model.Space; //

import java.util.ArrayList; //
import java.util.List; //
import java.util.Map; //
import java.util.Scanner; //
import java.util.stream.Stream; //

import static br.com.dio.util.BoardTemplate.BOARD_TEMPLATE; //
import static java.util.Objects.isNull; //
import static java.util.Objects.nonNull; //
import static java.util.stream.Collectors.toMap; //

public class Main {

    private final static Scanner scanner = new Scanner(System.in); //

    private static Board board; //

    private final static int BOARD_LIMIT = 9; //

    public static void main(String[] args) { //
        final var positions = Stream.of(args) //
                .collect(toMap( //
                        k -> k.split(";")[0], //
                        v -> v.split(";")[1] //
                ));
        var option = -1; //
        while (true){ //
            System.out.println("Selecione uma das opções a seguir"); //
            System.out.println("1 - Iniciar um novo Jogo"); //
            System.out.println("2 - Colocar um novo número"); //
            System.out.println("3 - Remover um número"); //
            System.out.println("4 - Visualizar jogo atual"); //
            System.out.println("5 - Verificar status do jogo"); //
            System.out.println("6 - limpar jogo"); //
            System.out.println("7 - Finalizar jogo"); //
            System.out.println("8 - Sair"); //

            option = scanner.nextInt(); //

            switch (option){ //
                case 1 -> startGame(positions); //
                case 2 -> inputNumber(); //
                case 3 -> removeNumber(); //
                case 4 -> showCurrentGame(); //
                case 5 -> showGameStatus(); //
                case 6 -> clearGame(); //
                case 7 -> finishGame(); //
                case 8 -> System.exit(0); //
                default -> System.out.println("Opção inválida, selecione uma das opções do menu"); //
            }
        }
    }

    private static void startGame(final Map<String, String> positions) { //
        if (nonNull(board)){ //
            System.out.println("O jogo já foi iniciado"); //
            return; //
        }

        List<List<Space>> spaces = new ArrayList<>(); //
        for (int i = 0; i < BOARD_LIMIT; i++) { // Percorre as LINHAS
            spaces.add(new ArrayList<>()); //
            for (int j = 0; j < BOARD_LIMIT; j++) { // Percorre as COLUNAS
                // O mapeamento de args deve ser "row,col" para corresponder ao acesso.
                // Ajuste a lógica aqui para assegurar que a string de entrada corresponda ao acesso (row, col)
                // Se sua string de entrada for "col,row", então o acesso deve ser spaces.get(j).get(i)
                // Assumindo que a string de entrada é "row,col"
                var positionConfig = positions.get("%s,%s".formatted(i, j)); //
                var expected = Integer.parseInt(positionConfig.split(",")[0]); //
                var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]); //
                var currentSpace = new Space(expected, fixed); //
                spaces.get(i).add(currentSpace); // Adiciona na linha 'i', coluna 'j'
            }
        }

        board = new Board(spaces); //
        System.out.println("O jogo está pronto para começar"); //
    }


    private static void inputNumber() { //
        if (isNull(board)){ //
            System.out.println("O jogo ainda não foi iniciado"); // Corrigido a duplicidade de "iniciado"
            return; //
        }

        System.out.println("Informe a linha que em que o número será inserido (0-8)");
        var row = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a coluna que em que o número será inserido (0-8)");
        var col = runUntilGetValidNumber(0, 8);
        System.out.printf("Informe o número que vai entrar na posição [%s,%s] (1-9)\n", row, col);
        var value = runUntilGetValidNumber(1, 9);
        
        // **AQUI: CHAMADA AO NOVO MÉTODO DE VALIDAÇÃO**
        if (board.getSpaces().get(row).get(col).isFixed()) {
             System.out.printf("A posição [%s,%s] tem um valor fixo e não pode ser alterada.\n", row, col);
             return;
        }

        // Tenta fazer a alteração, que agora inclui a validação de regras internas do Board
        if (!board.changeValue(col, row, value)){ //
            // A mensagem de erro agora vem do board.changeValue()
            // Ou você pode ter mensagens mais específicas aqui, se board.changeValue() retornar um enum de erro
        } else {
            System.out.println("Número inserido com sucesso!");
            showCurrentGame(); // Mostra o tabuleiro atualizado
        }
    }

    private static void removeNumber() { //
        if (isNull(board)){ //
            System.out.println("O jogo ainda não foi iniciado"); // Corrigido a duplicidade de "iniciado"
            return; //
        }

        System.out.println("Informe a linha do número a ser removido (0-8)");
        var row = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a coluna do número a ser removido (0-8)");
        var col = runUntilGetValidNumber(0, 8);
        
        if (!board.clearValue(col, row)){ //
            System.out.printf("A posição [%s,%s] tem um valor fixo e não pode ser limpa.\n", row, col); //
        } else {
            System.out.println("Número removido com sucesso!");
            showCurrentGame(); // Mostra o tabuleiro atualizado
        }
    }

    private static void showCurrentGame() { //
        if (isNull(board)){ //
            System.out.println("O jogo ainda não foi iniciado."); // Corrigido a duplicidade de "iniciado"
            return; //
        }

        var args = new Object[81];
        var argPos = 0;
        // CORREÇÃO AQUI: Iterar por LINHAS (i) e depois por COLUNAS (j) para preencher o template
        for (int i = 0; i < BOARD_LIMIT; i++) { // LINHAS
            for (int j = 0; j < BOARD_LIMIT; j++) { // COLUNAS
                var space = board.getSpaces().get(i).get(j); // Acessa spaces[linha][coluna]
                args[argPos ++] = " " + ((isNull(space.getActual())) ? " " : space.getActual());
            }
        }
        System.out.println("Seu jogo se encontra da seguinte forma"); //
        System.out.printf((BOARD_TEMPLATE) + "\n", args); //
    }

    private static void showGameStatus() { //
        if (isNull(board)){ //
            System.out.println("O jogo ainda não foi iniciado."); // Corrigido a duplicidade de "iniciado"
            return; //
        }

        System.out.printf("O jogo atualmente se encontra no status %s\n", board.getStatus().getLabel()); //
        if(board.hasErrors()){ //
            System.out.println("O jogo contém erros de solução ou de regras do Sudoku!"); // Mensagem mais específica
        } else { //
            System.out.println("O jogo não contém erros."); //
        }
    }

    private static void clearGame() { //
        if (isNull(board)){ //
            System.out.println("O jogo ainda não foi iniciado."); // Corrigido a duplicidade de "iniciado"
            return; //
        }

        System.out.println("Tem certeza que deseja limpar seu jogo e perder todo seu progresso? (sim/não)"); // Mais claro
        var confirm = scanner.next(); //
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")){ //
            System.out.println("Informe 'sim' ou 'não'"); //
            confirm = scanner.next(); //
        }

        if(confirm.equalsIgnoreCase("sim")){ //
            board.reset(); //
            System.out.println("Jogo limpo com sucesso!");
            showCurrentGame(); // Mostra o tabuleiro limpo
        } else {
            System.out.println("Operação de limpeza cancelada.");
        }
    }

    private static void finishGame() { //
        if (isNull(board)){ //
            System.out.println("O jogo ainda não foi iniciado."); // Corrigido a duplicidade de "iniciado"
            return; //
        }

        if (board.gameIsFinished()){ //
            System.out.println("Parabéns você concluiu o jogo!"); //
            showCurrentGame(); //
            board = null; //
            System.out.println("Iniciando um novo jogo. Selecione a opção 1 para começar.");
        } else if (board.hasErrors()) { //
            System.out.println("Seu jogo contém erros de solução ou de regras do Sudoku. Verifique seu tabuleiro e ajuste-o."); //
        } else { //
            System.out.println("Você ainda precisa preencher algum espaço para completar o jogo."); //
        }
    }


    private static int runUntilGetValidNumber(final int min, final int max){ //
        while (!scanner.hasNextInt()) { // Valida se é um número
            System.out.println("Entrada inválida. Por favor, digite um número.");
            scanner.next(); // Consome a entrada inválida
        }
        var current = scanner.nextInt(); //
        while (current < min || current > max){ //
            System.out.printf("Informe um número entre %s e %s\n", min, max); //
            while (!scanner.hasNextInt()) { // Valida se é um número
                System.out.println("Entrada inválida. Por favor, digite um número.");
                scanner.next(); // Consome a entrada inválida
            }
            current = scanner.nextInt(); //
        }
        return current; //
    }

}
