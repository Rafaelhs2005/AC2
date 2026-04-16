// DEFINIÇÃO DOS PINOS
int led_red    = 13; 
int led_yellow = 12;
int led_green  = 11;
int led_blue   = 10; 

// VARIÁVEIS GLOBAIS
String memory[100];   
int ic = 0;           // Contador de instruções carregadas na memória
int PC = 0;           // Program Counter (Índice para o vetor memory)

char regW = '0';      
char regX = '0';      
char regY = '0';      

void setup(void) {
    Serial.begin(9600); 
    pinMode(led_red, OUTPUT);
    pinMode(led_yellow, OUTPUT);
    pinMode(led_green, OUTPUT);
    pinMode(led_blue, OUTPUT);
    Serial.println("Pronto para receber o arquivo .hex...");
}

void loop(void) {
    // 1) O programa espera carregar todas as instruções primeiro
    if (Serial.available() > 0) {
        String input = Serial.readString();
        input.trim();
        
        // Reset de variáveis para nova execução
        ic = 0;
        PC = 0;
        
        // Passo A: Carrega todas as instruções para a memória (vetor)
        load_to_memory(input);
        
        // Passo B: Só inicia a execução após o carregamento completo
        execute_all();
    }
}

void load_to_memory(String input) {
    int i = 0;
    while (i < input.length()) {
        if (input.charAt(i) <= 32) { i++; continue; } // Pula espaços
        
        if (ic < 100) {
            memory[ic] = input.substring(i, i + 3); // Salva XYW no vetor
            ic++;
            i += 4; // Avança para a próxima (XYW + espaço)
        } else { break; }
    }
    Serial.print(ic);
    Serial.println(" instrucoes carregadas com sucesso. Iniciando execucao...");
}

void execute_all() {
    while (PC < ic) {
        String currentInstruction = memory[PC]; 

        regX = currentInstruction.charAt(0);
        regY = currentInstruction.charAt(1);
        char opcode = currentInstruction.charAt(2);

        // Processamento na ULA
        String resHex = do_ula(regX, regY, opcode);
        regW = resHex.charAt(0); 

        // Hardware (LEDs)
        update_leds(hexchar_to_int(regW));

        print_status();

        delay(3000); // Pausa para leitura
        PC++; // Incrementa o indexador
    }
    Serial.println(">>> Execucao finalizada.");
}

void print_status() {
    // Exibe o vetor memória de forma clara
    Serial.print("Memoria: ");
    for (int i = 0; i < ic; i++) {
        if (i == PC) Serial.print("->"); // Indica onde o PC está apontando
        Serial.print("| ");
        Serial.print(memory[i]);
        Serial.print(" ");
    }
    Serial.println("|");

    // Exibe registradores: PC, W, X, Y
    Serial.print("Registradores: |  ");
    Serial.print(String(PC, HEX)); 
    Serial.print(" |  ");
    Serial.print(regW);
    Serial.print(" |  ");
    Serial.print(regX);
    Serial.print(" |  ");
    Serial.print(regY);
    Serial.println(" |");
    
    Serial.println("--"); 
}

// --- Funções de Apoio (ULA e Conversões) ---

String do_ula(char X, char Y, char W) {
    int A = hexchar_to_int(X);
    int B = hexchar_to_int(Y);
    int res = 0;
    switch (W) {
        case '0': res = A; break;
        case '1': res = B; break;
        case '2': res = A & B; break;
        case '3': res = (~(A & B)) & 0xF; break;
        case '4': res = A & (~B); break;
        case '5': res = (~B) & 0xF; break;
        case '6': res = ((~A) | (~B)) & 0xF; break;
        case '7': res = (~A) & 0xF; break;
        case '8': res = A | (~B); break;
        case '9': res = 1; break;
        case 'A': res = 0; break;
        case 'B': res = A & B; break;
        case 'C': res = ((~A) & B) & 0xF; break;
        case 'D': res = A & (~B); break;
        case 'E': res = A | B; break;
        case 'F': res = ((~A) & (~B)) & 0xF; break;
        default: res = 0; break;
    }
    String s = String(res, HEX);
    s.toUpperCase();
    return s;
}

int hexchar_to_int(char hexc) {
    if (hexc >= '0' && hexc <= '9') return hexc - '0';
    if (hexc >= 'A' && hexc <= 'F') return 10 + (hexc - 'A');
    if (hexc >= 'a' && hexc <= 'f') return 10 + (hexc - 'a');
    return 0;
}

void update_leds(int result) {
    for (int i = 0; i < 4; i++) {
        digitalWrite(10 + i, (result >> i) & 1);
    }
}