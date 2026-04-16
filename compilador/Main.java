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
        // Ajuste os caminhos dos arquivos conforme sua pasta
        File arquivoEntrada = new File("compilador/testeula.ula");
        File arquivoSaida = new File("compilador/testeula.hex");

        try {
            BufferedReader arq = new BufferedReader(new FileReader(arquivoEntrada));
            BufferedWriter saida = new BufferedWriter(new FileWriter(arquivoSaida));

            String linha;
            String X = "";
            String Y = "";

            while ((linha = arq.readLine()) != null) {
                linha = linha.trim();

                if (linha.isEmpty() || linha.equals("inicio:")) {
                    continue;
                } else if (linha.startsWith("X")) {
                    X = linha.split("=")[1].replace(";", "").trim();
                } else if (linha.startsWith("Y")) {
                    Y = linha.split("=")[1].replace(";", "").trim();
                } else if (linha.startsWith("W")) {
                    if (X.isEmpty() || Y.isEmpty()) {
                        System.out.println("Erro: X ou Y não definidos.");
                        break;
                    } else {
                        String mnemônicoLido = linha.split("=")[1].replace(";", "").trim();
                        String valorHex = procurarMnemonico(mnemônicoLido);

                        if (valorHex == null) {
                            saida.write("Erro");
                            System.out.println("Erro: Mnemónico '" + mnemônicoLido + "' não reconhecido.");
                            break; 
                        }

                        try {
                            // CORREÇÃO: Usar base 16 para aceitar A, B, C, D, E, F
                            int valX = Integer.parseInt(X, 16);
                            int valY = Integer.parseInt(Y, 16);
                            
                            // Escreve o código XYW e um espaço (para o Arduino ler de 4 em 4 caracteres)
                            saida.write(String.format("%X%X%s ", valX, valY, valorHex));
                        } catch (NumberFormatException e) {
                            System.out.println("Erro ao converter valor: " + X + " ou " + Y);
                            saida.write("Erro");
                            break;
                        }
                    }
                } else if (linha.equals("fim.")) {
                    break;
                }
            }

            arq.close();
            saida.close();
            System.out.println("Compilação finalizada com sucesso!");

        } catch (IOException e) {
            System.out.println("Erro ao processar os arquivos: " + e.getMessage());
        }
    }
}