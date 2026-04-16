import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static String procurarMnemonico(String string) {
        Map<String, String> tabela = new HashMap<>();

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

        return tabela.get(string);
    }

    public static void main(String[] args) {
        String nomeArquivoEntrada = (args.length > 0) ? args[0] : "testeula.ula";
        File arquivoEntrada = new File(nomeArquivoEntrada);
        File arquivoSaida = new File(nomeArquivoEntrada.replace(".ula", ".hex"));

        try {
            BufferedReader arq = new BufferedReader(new FileReader(arquivoEntrada));
            BufferedWriter saida = new BufferedWriter(new FileWriter(arquivoSaida));

            StringBuilder conteudoCompleto = new StringBuilder();
            String l;
            while ((l = arq.readLine()) != null) {
                conteudoCompleto.append(l).append("\n");
            }
            String codigo = conteudoCompleto.toString();

            int indexInicio = codigo.indexOf("inicio:");
            int indexFim = codigo.lastIndexOf("fim.");

            if (indexInicio == -1) {
                System.out.println("Erro de sintaxe: Bloco 'inicio:' não encontrado.");
                arquivoSaida.delete();
                return;
            }
            if (indexFim == -1) {
                System.out.println("Erro de sintaxe: Bloco 'fim.' não encontrado.");
                arquivoSaida.delete();
                return;
            }
            if (indexInicio > indexFim) {
                System.out.println("Erro de sintaxe: 'inicio:' deve vir antes de 'fim.'.");
                arquivoSaida.delete();
                return;
            }

            // Garante que não há código útil antes do inicio:
            String antesDoInicio = codigo.substring(0, indexInicio).trim();
            if (!antesDoInicio.isEmpty()) {
                System.out.println("Erro de sintaxe: Código detectado antes do bloco 'inicio:'.");
                arquivoSaida.delete();
                return;
            }

            // Extrai o conteúdo entre inicio: e fim.
            String corpo = codigo.substring(indexInicio + 7, indexFim);
            
            // Verifica se o corpo termina com ';' (ignorando espaços e quebras de linha)
            if (!corpo.trim().isEmpty() && !corpo.trim().endsWith(";")) {
                System.out.println("Erro de sintaxe: Toda instrução deve terminar com ';'.");
                arquivoSaida.delete();
                return;
            }

            String[] segmentos = corpo.split(";");
            boolean houveErro = false;
            String X = "";
            String Y = "";

            for (String segmento : segmentos) {
                String instrucao = segmento.trim();
                if (instrucao.isEmpty()) continue;

                if (!instrucao.contains("=")) {
                    System.out.println("Erro de sintaxe: Instrução malformada '" + instrucao + "' (falta '=').");
                    houveErro = true;
                    break;
                }

                String[] partes = instrucao.split("=");
                if (partes.length != 2) {
                    System.out.println("Erro de sintaxe: Instrução malformada '" + instrucao + "' (esperado VAR=VALOR).");
                    houveErro = true;
                    break;
                }

                String variavel = partes[0].trim();
                String valor = partes[1].trim();

                if (valor.isEmpty()) {
                    System.out.println("Erro de sintaxe: Valor de atribuição vazio em '" + instrucao + "'.");
                    houveErro = true;
                    break;
                }

                if (variavel.equals("X")) {
                    X = valor;
                } else if (variavel.equals("Y")) {
                    Y = valor;
                } else if (variavel.equals("W")) {
                    if (X.isEmpty() || Y.isEmpty()) {
                        System.out.println("Erro: X ou Y não definidos antes de W na instrução '" + instrucao + "'.");
                        houveErro = true;
                        break;
                    } else {
                        String valorHex = procurarMnemonico(valor);
                        if (valorHex == null) {
                            System.out.println("Erro: Mnemônico '" + valor + "' não reconhecido.");
                            houveErro = true;
                            break; 
                        }

                        try {
                            int valX = Integer.parseInt(X.trim(), 16);
                            int valY = Integer.parseInt(Y.trim(), 16);
                            saida.write(String.format("%X%X%s ", valX, valY, valorHex));
                        } catch (NumberFormatException e) {
                            System.out.println("Erro ao converter valor: " + X + " ou " + Y);
                            houveErro = true;
                            break;
                        }
                    }
                } else {
                    System.out.println("Erro de sintaxe: Variável '" + variavel + "' desconhecida.");
                    houveErro = true;
                    break;
                }
            }

            arq.close();
            saida.close();

            if (!houveErro) {
                System.out.println("Compilação finalizada com sucesso!");
            } else {
                arquivoSaida.delete();
            }

        } catch (IOException e) {
            System.out.println("Erro ao processar os arquivos: " + e.getMessage());
        }
    }
}