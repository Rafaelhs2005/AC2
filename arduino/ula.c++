// DEFINIÇÃO DOS PINOS
int led_red    = 13; 
int led_yellow = 12;
int led_green  = 11;
int led_blue   = 10; 

// VARIÁVEIS GLOBAIS
String memory[100];   
int ic = 0;           // Contador de instruções carregadas na memória

// VETOR DE REGISTRADORES
// [0] = PC (em int), [1] = W (char), [2] = X (char), [3] = Y (char)
String registradores[4]; 

void setup(void) {
    Serial.begin(9600); 
    pinMode(led_red, OUTPUT);
    pinMode(led_yellow, OUTPUT);
    pinMode(led_green, OUTPUT);
    pinMode(led_blue, OUTPUT);
    
    // Inicialização dos registradores com valores padrão
    registradores[0] = "0"; // PC
    registradores[1] = "0"; // W
    registradores[2] = "0"; // X
    registradores[3] = "0"; // Y
    
    Serial.println("Pronto para receber o arquivo .hex...");
}

void loop(void) {
    if (Serial.available() > 0) {
        String input = Serial.readString();
        input.trim();
        
        // Reset para nova execução
        ic = 0;
        registradores[0] = "0"; // PC volta a 0
        
        load_to_memory(input);
        execute_all();
    }
}

void load_to_memory(String input) {
    int i = 0;
    while (i < input.length()) {
        if (input.charAt(i) <= 32) { i++; continue; }
        
        if (ic < 100) {
            memory[ic] = input.substring(i, i + 3);
            ic++;
            i += 4; 
        } else { break; }
    }
    Serial.print(ic);
    Serial.println(" instrucoes carregadas. Iniciando...");
}

void execute_all() {
    // Converte o valor do PC guardado no vetor para inteiro para controlar o loop
    int pc_atual = registradores[0].toInt();

    while (pc_atual < ic) {
        // Indexando o vetor memória através do valor lógico do PC
        String currentInstruction = memory[pc_atual]; 

        // Atribuindo valores aos registradores no vetor
        registradores[2] = String(currentInstruction.charAt(0)); // X
        registradores[3] = String(currentInstruction.charAt(1)); // Y
        char opcode      = currentInstruction.charAt(2);

        // Processamento na ULA
        String resHex = do_ula(registradores[2].charAt(0), registradores[3].charAt(0), opcode);
        registradores[1] = resHex; // W

        // Hardware (LEDs) usando o valor de W
        update_leds(hexchar_to_int(registradores[1].charAt(0)));

        print_status();

        delay(3000); 
        
        // Atualiza o PC no vetor
        pc_atual++;
        registradores[0] = String(pc_atual);
    }
    Serial.println(">>> Execucao finalizada.");
}

void print_status() {
    int pc_print = registradores[0].toInt();
    
    Serial.print("Memoria: ");
    for (int i = 0; i < ic; i++) {
        if (i == pc_print) Serial.print("->"); 
        Serial.print("| ");
        Serial.print(memory[i]);
        Serial.print(" ");
    }
    Serial.println("|");

    // Exibe registradores: PC [0], W [1], X [2], Y [3]
    Serial.print("Registradores: |  ");
    Serial.print(String(pc_print, HEX)); // PC em Hexa
    Serial.print(" |  ");
    Serial.print(registradores[1]);      // W
    Serial.print(" |  ");
    Serial.print(registradores[2]);      // X
    Serial.print(" |  ");
    Serial.print(registradores[3]);      // Y
    Serial.println(" |");
    
    Serial.println("--"); 
}

// --- Funções de Apoio ---

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
