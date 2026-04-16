// DEFINIÇÃO DOS PINOS
int led_red    = 13; 
int led_yellow = 12;
int led_green  = 11;
int led_blue   = 10; 

// VARIÁVEIS GLOBAIS
String memory[100];   
String input = "";    

int ic = 0;           // Contador de instruções lidas
int PC = 0;           // Program Counter

char regW = '0';      
char regX = '0';      
char regY = '0';      

void setup(void) {
    Serial.begin(9600); 
    pinMode(led_red, OUTPUT);
    pinMode(led_yellow, OUTPUT);
    pinMode(led_green, OUTPUT);
    pinMode(led_blue, OUTPUT);
}

void loop(void) {
    if (Serial.available() > 0) {
        input = Serial.readString();
        input.trim();
        
        // Reset para nova execução
        ic = 0;
        PC = 0; 
        
        load_instruction_to_memory(input); 
        execute_instruction(); 
    }
}

void load_instruction_to_memory(String input) {   
    int size = input.length();
    int i = 0; 
    while (i < size) {
        // Ignora espaços ou caracteres de controle
        if (input.charAt(i) <= 32) { i++; continue; }
        
        if (ic < 100) {
            // Captura o bloco XYW
            memory[ic] = input.substring(i, i + 3);
            ic++;      
            i += 4; // Avança para a próxima instrução (considerando o espaço)
        } else { break; }
    }
}

void execute_instruction(void) {
    while (PC < ic) {
        String instruction = memory[PC]; 

        regX = instruction.charAt(0);
        regY = instruction.charAt(1);
        char opcode = instruction.charAt(2);

        String resHex = do_instruction(regX, regY, opcode);
        regW = resHex.charAt(0); 

        update_leds(hexchar_to_int(regW));

        // CHAMA A IMPRESSÃO FORMATADA
        print_formatted_output();

        delay(4000); 
        PC++; 
    }
    Serial.println("End of instructions!");
}

// FUNÇÃO DE IMPRESSÃO QUE MOSTRA APENAS O QUE FOI CARREGADO
void print_formatted_output() {
    // Linha da Memória: percorre apenas até 'ic' (instruções carregadas)
    Serial.print("Memoria: | ");
    for (int i = 0; i < ic; i++) {
        Serial.print(memory[i]);
        Serial.print(" | ");
    }
    Serial.println();

    // Linha dos Registradores: | PC | W | X | Y |
    Serial.print("Registradores: | ");
    Serial.print(String(PC, HEX)); 
    Serial.print(" | ");
    Serial.print(regW);
    Serial.print(" | ");
    Serial.print(regX);
    Serial.print(" | ");
    Serial.print(regY);
    Serial.println(" | ");
    
    Serial.println("--"); 
}

String do_instruction(char X, char Y, char W) {
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
        int bit = (result >> i) & 1;
        if (i == 0) digitalWrite(led_blue, bit);
        if (i == 1) digitalWrite(led_green, bit);
        if (i == 2) digitalWrite(led_yellow, bit);
        if (i == 3) digitalWrite(led_red, bit);
    }
}