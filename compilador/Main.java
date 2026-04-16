import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    // Função que converte um mnemônico para seu valor hexadecimal correspondente
    public static String procurarMnemonico(String string) {
        // Cria uma tabela de associação (mnemônico -> código HEX)
        Map<String, String> tabela = new HashMap<>();

        // Preenche a tabela com todas as operações da ULA
        tabela.put("CopiaA", "0");
        tabela.put("CopiaB", "1");
        tabela.put("AxB", "2");
        tabela.put("nAxnB", "3");
        tabela.put("AeBn", "4");
        tabela.put("nB", "5");
        tabela.put("nAonB", "6");
        tabela.put("nA", "7");
        tabela.put("AonB", "8");
        tabela.put("UmL", "9");
        tabela.put("ZeroL", "A");
        tabela.put("AeB", "B");
        tabela.put("nAeB", "C");
        tabela.put("AenB", "D");
        tabela.put("AoB", "E");
        tabela.put("nAenB", "F");

        // Retorna o valor correspondente ao mnemônico informado
        // Se não existir, retorna null
        return tabela.get(string);
    }

    public static void main(String[] args) {

        // Define o arquivo de entrada:
        // se o usuário passar argumento, usa ele; senão usa padrão
        String nomeArquivoEntrada = (args.length > 0) ? args[0] : "compilador/testeula.ula";

        // Cria objeto do arquivo de entrada
        File arquivoEntrada = new File(nomeArquivoEntrada);

        // Cria arquivo de saída substituindo .ula por .hex
        File arquivoSaida = new File(nomeArquivoEntrada.replace(".ula", ".hex"));

        try {
            // Abre arquivo para leitura
            BufferedReader arq = new BufferedReader(new FileReader(arquivoEntrada));

            // Abre arquivo para escrita
            BufferedWriter saida = new BufferedWriter(new FileWriter(arquivoSaida));

            // Lê TODO o conteúdo do arquivo para uma string
            StringBuilder conteudoCompleto = new StringBuilder();
            String l;
            while ((l = arq.readLine()) != null) {
                conteudoCompleto.append(l).append("\n");
            }
            String codigo = conteudoCompleto.toString();

            // Procura as posições de "inicio:" e "fim."
            int indexInicio = codigo.indexOf("inicio:");
            int indexFim = codigo.lastIndexOf("fim.");

            // Validação: início não encontrado
            if (indexInicio == -1) {
                System.out.println("Erro de sintaxe: Bloco 'inicio:' não encontrado.");
                arquivoSaida.delete();
                return;
            }

            // Validação: fim não encontrado
            if (indexFim == -1) {
                System.out.println("Erro de sintaxe: Bloco 'fim.' não encontrado.");
                arquivoSaida.delete();
                return;
            }

            // Validação: ordem incorreta
            if (indexInicio > indexFim) {
                System.out.println("Erro de sintaxe: 'inicio:' deve vir antes de 'fim.'.");
                arquivoSaida.delete();
                return;
            }

            // Verifica se existe código antes do "inicio:"
            String antesDoInicio = codigo.substring(0, indexInicio).trim();
            if (!antesDoInicio.isEmpty()) {
                System.out.println("Erro de sintaxe: Código detectado antes do bloco 'inicio:'.");
                arquivoSaida.delete();
                return;
            }

            // Extrai apenas o conteúdo entre inicio: e fim.
            String corpo = codigo.substring(indexInicio + 7, indexFim);

            // Verifica se todas as instruções terminam com ';'
            if (!corpo.trim().isEmpty() && !corpo.trim().endsWith(";")) {
                System.out.println("Erro de sintaxe: Toda instrução deve terminar com ';'.");
                arquivoSaida.delete();
                return;
            }

            // Divide o código em instruções separadas por ';'
            String[] segmentos = corpo.split(";");

            boolean houveErro = false;

            // Variáveis que armazenam os valores atuais de X e Y
            String X = "";
            String Y = "";

            // Percorre cada instrução
            for (String segmento : segmentos) {

                String instrucao = segmento.trim();

                // Ignora instruções vazias
                if (instrucao.isEmpty()) continue;

                // Verifica se existe '='
                if (!instrucao.contains("=")) {
                    System.out.println("Erro de sintaxe: Instrução malformada '" + instrucao + "'.");
                    houveErro = true;
                    break;
                }

                // Divide em variável e valor
                String[] partes = instrucao.split("=");
                String variavel = partes[0].trim();
                String valor = partes[1].trim();

                // Atribuição para X
                if (variavel.equals("X")) {
                    X = valor;

                // Atribuição para Y
                } else if (variavel.equals("Y")) {
                    Y = valor;

                // Operação da ULA
                } else if (variavel.equals("W")) {

                    // Verifica se X e Y já foram definidos
                    if (X.isEmpty() || Y.isEmpty()) {
                        System.out.println("Erro: X ou Y não definidos.");
                        houveErro = true;
                        break;
                    }

                    // Converte mnemônico para HEX
                    String valorHex = procurarMnemonico(valor);

                    // Verifica se o mnemônico existe
                    if (valorHex == null) {
                        System.out.println("Erro: Mnemônico '" + valor + "' não reconhecido.");
                        houveErro = true;
                        break;
                    }

                    try {
                        // Converte X e Y (entrada em decimal)
                        int valX = Integer.parseInt(X);
                        int valY = Integer.parseInt(Y);

                        // Garante que os valores fiquem entre 0 e F (1 dígito HEX)
                        valX = valX & 0xF;
                        valY = valY & 0xF;

                        // Escreve no arquivo no formato: XYZ (hexadecimal)
                        saida.write(String.format("%X%X%s ", valX, valY, valorHex));

                    } catch (NumberFormatException e) {
                        // Erro caso X ou Y não sejam números válidos
                        System.out.println("Erro ao converter valor: " + X + " ou " + Y);
                        houveErro = true;
                        break;
                    }

                } else {
                    // Caso a variável não seja X, Y ou W
                    System.out.println("Erro: Variável '" + variavel + "' desconhecida.");
                    houveErro = true;
                    break;
                }
            }

            // Fecha os arquivos
            arq.close();
            saida.close();

            // Se não houve erro, sucesso
            if (!houveErro) {
                System.out.println("Compilação finalizada com sucesso!");
            } else {
                // Se houve erro, apaga o arquivo gerado
                arquivoSaida.delete();
            }

        } catch (IOException e) {
            // Trata erro de leitura/escrita de arquivos
            System.out.println("Erro ao processar os arquivos: " + e.getMessage());
        }
    }
}