import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다 또한 instruction 관련 연산,
 * 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/**
	 * inst.data 파일을 불러와 저장하는 공간. 명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * 
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */

	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
		instMap.get(null);
	}
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		// ...
		BufferedReader file = null;
		try {
			file = new BufferedReader(new FileReader (new File(fileName)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		
		try {
			while ((line = file.readLine()) != null){
				Instruction inst = new Instruction(line);
				instMap.put(inst.instruction, inst);
			}
			file.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	// get, set, search 등의 함수는 자유 구현
	public int getOpcode(String instruction) {
		if (instruction == null)
			return -1;
		if (instMap.containsKey(instruction))
			return instMap.get(instruction).opcode;
		else 
			return -2;
	}
	public int getFormat(String instruction) {
		int format = 0;
		if (instruction == null)
			return -1;
		if (instMap.containsKey(instruction))
			format = instMap.get(instruction).format;
		else 
			return -2;
		if (instruction.charAt(0)=='+')
			format++;
		return format;
	}
	public int getNO(String instruction) {
		if (instruction == null)
			return -1;
		if (instMap.containsKey(instruction))
			return instMap.get(instruction).numberOfOperand;
		else 
			return -2;
	}
}

/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다. instruction과 관련된 정보를 저장하고 기초적인 연산을
 * 수행한다.
 */
class Instruction {

	/*
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 * 
	 * ex) String instruction; int opcode; int numberOfOperand; String comment;
	 */
	String instruction;
	int opcode;
	int numberOfOperand;
	/** instruction이 몇 바이트 명령어인지 저장 */
	int format;
	

	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 **/
	public void parsing(String line) {
		// TODO Auto-generated method stub
		
		StringTokenizer token= new StringTokenizer(line, "\t");
		this.instruction = token.nextToken();
		this.opcode = Integer.parseInt((token.nextToken()),16);
		this.format = Integer.parseInt(token.nextToken()); 
		this.numberOfOperand = Integer.parseInt(token.nextToken());
	}


}
