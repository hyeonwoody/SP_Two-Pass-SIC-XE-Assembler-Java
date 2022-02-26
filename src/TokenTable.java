import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다.
 * 
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다. section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;
	
	public static final int AReg = 0;
	public static final int XReg = 1;
	public static final int LReg = 2;
	public static final int BReg = 3;
	public static final int SReg = 4;
	public static final int TReg = 5;
	public static final int FReg = 6;
	public static final int PCReg = 8;
	public static final int SWReg = 9;

	public static int def_index = 0;
	public static int ref_index = 0;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	LabelTable symTab;
	LabelTable literalTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	ArrayList<Modification> modifTab;

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab    : 해당 section과 연결되어있는 symbol table
	 * @param literaTab : 해당 section과 연결되어있는 literal table
	 * @param instTab   : instruction 명세가 정의된 instTable
	 */
	public TokenTable(LabelTable symTab, LabelTable literalTab, InstTable instTab) {
		// ...
		this.tokenList = new ArrayList<Token>();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab; 
		
		modifTab = new ArrayList <Modification>();
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		// ...
		int E_addr =0;
		String code ="";
		String tmp_operator = "";
		int tmp = 0;
		int register =0;
		int result =0;
		
		if (tokenList.get(index).operator.equals("START") ||
			tokenList.get(index).operator.equals("CSECT")) {
			E_addr = 0;
			tokenList.get(index).record = 'H';
			code =	"H"
					+String.format("%-6s", symTab.label.get(0))
					+String.format("%06X", tokenList.get(index).byteSize);
		}
		else if (tokenList.get(index).operator.equals("EXTDEF")) {
			def_index = index;
			tokenList.get(index).record = 'D';
			code ="D";
			for (int i=0; i<tokenList.get(index).operand.length; i++) {
				code +=String.format("%-6s", tokenList.get(index).operand[i])
						+String.format("%06X", symTab.search(tokenList.get(index).operand[i]));
			}
		}
		else if (tokenList.get(index).operator.equals("EXTREF")) {
			ref_index = index;
			tokenList.get(index).record = 'R';
			code = "R";
			for (int i=0; i<tokenList.get(index).operand.length; i++) {
				code +=String.format("%-6s", tokenList.get(index).operand[i]);
				
			}
				
		}
		if (tokenList.get(index).operator.charAt(0)=='+') {
			tmp_operator = tokenList.get(index).operator.substring(1);
		}
		else {
			tmp_operator = tokenList.get(index).operator;
		}
		if ((instTab.instMap.get(tmp_operator)) != null) {
			if (instTab.getFormat(tmp_operator) ==2) {
				tokenList.get(index).setFlag(nFlag, 0);
				tokenList.get(index).setFlag(iFlag, 0);
			} 
			//
			else if ((tokenList.get(index).operand[0]!="") && tokenList.get(index).operand[0].charAt(0) == '#') {
				tokenList.get(index).setFlag(nFlag, 0);
				tokenList.get(index).setFlag(iFlag, 1);
			}
			else if ((tokenList.get(index).operand[0]!="") && tokenList.get(index).operand[0].charAt(0) == '@') {
				tokenList.get(index).setFlag(nFlag, 1);
				tokenList.get(index).setFlag(iFlag, 0);
			}
			else {
				tokenList.get(index).setFlag(nFlag, 1);
				tokenList.get(index).setFlag(iFlag, 1);
			}
				if (tokenList.get(index).operand.length>1 && tokenList.get(index).operand[1].charAt(0) == 'X') {
					tokenList.get(index).setFlag(xFlag, 1);
				}
				if (tokenList.get(index).operator.charAt(0) == '+') {
					tokenList.get(index).setFlag(eFlag, 1);
				}
				for (int i=0; i<MAX_OPERAND; i++) {
					if (tokenList.get(ref_index).operand.length>i && tokenList.get(ref_index).operand[i].equals(tokenList.get(index).operand[0])) {
						//tokenList.get(index).nixbpe = 0;
					}
					else if (tokenList.get(index).operator.charAt(0) != '+' && tokenList.get(index).operand[0] != "") {
						tokenList.get(index).setFlag(pFlag, 1);
						break;
					}
				}
				if (tokenList.get(index).operator.charAt(0) == '#') {
					tokenList.get(index).setFlag(pFlag, 0);
				}
					
					switch (tokenList.get(index).byteSize) {
					case 2:
						tmp = instTab.getOpcode(tmp_operator) << 8;
						
						for (int i =0; i<tokenList.get(index).operand.length; i++) {

							if (tokenList.get(index).operand[i].equals("A")) {
								register|= AReg;
							} else if (tokenList.get(index).operand[i].equals("X")) {
								register|= XReg;
							} else if (tokenList.get(index).operand[i].equals("L")) {
								register|= LReg;
							} else if (tokenList.get(index).operand[i].equals("B")) {
								register|= BReg;
							} else if (tokenList.get(index).operand[i].equals("S")) {
								register|= SReg;
							} else if (tokenList.get(index).operand[i].equals("T")) {
								register|= TReg;
							} else if (tokenList.get(index).operand[i].equals("F")) {
								register|= FReg;
							} else if (tokenList.get(index).operand[i].equals("PC")) {
								register|= PCReg;
							} else if (tokenList.get(index).operand[i].equals("SW")) {
								register|= SWReg;
							}
							if (i == 0)
								register = register<<4;
							
						}
						tmp |= register;
						code = String.format("%04X", tmp).toUpperCase();
						break;
					case 3:
						tmp |= instTab.getOpcode(tmp_operator) << 16;
						tmp|=tokenList.get(index).getFlag(nFlag) + tokenList.get(index).getFlag(iFlag)<<12;
						//System.out.println("This"+tokenList.get(index).operand[0]);
						if (tokenList.get(index).operand[0]!="" && tokenList.get(index).operand[0].charAt(0) == '#') {
							tokenList.get(index).setFlag(pFlag, 0);
							tmp|=tokenList.get(index).getFlag(pFlag) <<12;
							tmp |= Integer.parseInt(tokenList.get(index).operand[0].substring(1));
						}
						else {
							tokenList.get(index).setFlag(pFlag, 1);
							if ((result = symTab.search(tokenList.get(index).operand[0])) >= 0){
								tmp|=tokenList.get(index).getFlag(pFlag) <<12;
								tmp |= result - (tokenList.get(index).location + tokenList.get(index).byteSize) & 0xFFF; 
								
							}
							else if((result = literalTab.search(tokenList.get(index).operand[0])) >= 0){
								tmp|=tokenList.get(index).getFlag(pFlag) <<12;
								tmp |= result - (tokenList.get(index).location + tokenList.get(index).byteSize) & 0xFFF; 
							}
							else if (tokenList.get(index).operator.equals("RSUB")){
								tokenList.get(index).setFlag(pFlag, 0);
								tmp|=tokenList.get(index).getFlag(pFlag) <<12;
							}
							else {
								tmp|=tokenList.get(index).getFlag(pFlag) <<12;
							}
						}
						
						code = String.format("%06X", tmp).toUpperCase();
								//+ Integer.toHexString (tokenList.get(index).getFlag(xFlag) + tokenList.get(index).getFlag(bFlag)
										//+ tokenList.get(index).getFlag(pFlag) + tokenList.get(index).getFlag(eFlag)<<8);
						break;
					case 4:
						tokenList.get(index).setFlag(TokenTable.eFlag, 1);
						tmp = instTab.getOpcode(tmp_operator)<<24;
						tmp|=tokenList.get(index).getFlag(nFlag) + tokenList.get(index).getFlag(iFlag) 
								+tokenList.get(index).getFlag(xFlag) + tokenList.get(index).getFlag(bFlag)
								+ tokenList.get(index).getFlag(pFlag) + tokenList.get(index).getFlag(eFlag) <<20;
						code = String.format("%08X", tmp).toUpperCase();
						modifTab.add(new Modification (tokenList.get(index).location+1, 5, '+', tokenList.get(index).operand[0]));
					}
		}
		else if (tokenList.get(index).operator.equals("BYTE")) {
			StringTokenizer token= new StringTokenizer(tokenList.get(index).operand[0], "'");
			if (token.nextToken().equals("X")) 
				tmp = Integer.parseInt(token.nextToken(), 16);
			else {
				String value = token.nextToken();
				for (int i=0; i<value.length(); i++) {
					tmp = tmp << 8;
					tmp |= value.charAt(i);
				}
			}
			switch (tokenList.get(index).byteSize) {
			case 1:
				code = String.format("%02X", tmp).toUpperCase();
				break;
			case 2:
				code = String.format("%04X", tmp).toUpperCase();
				break;
			case 3:
				code = String.format("%06X", tmp).toUpperCase();
				break;
			case 4:
				code = String.format("%08X", tmp).toUpperCase();
				break;
			}
		}
		else if (tokenList.get(index).operator.equals("WORD")) {
			if (tokenList.get(index).operand[0].contains("-")) {
				StringTokenizer token= new StringTokenizer(tokenList.get(index).operand[0], "-");
				modifTab.add(new Modification (tokenList.get(index).location, 6, '+', token.nextToken()));
				modifTab.add(new Modification (tokenList.get(index).location, 6, '-', token.nextToken()));
				code = "000000";
			}
		}
		
		else if (tokenList.get(index).operator.equals("LTORG")||
				tokenList.get(index).operator.equals("END")) {
			for (String label : literalTab.label) {
				switch (label.charAt(1)) {
				case 'X':
					tmp = Integer.parseInt(label.substring(3, label.length()-1), 16);
					break;
				case 'C':
					for (int i=3; i<label.length()-1; i++) {
						tmp = tmp << 8;
						tmp |= label.charAt(i);
					}
					break;
				default :
					tmp = Integer.parseInt(label.substring(1), 16);
					break;
				}
				switch (tokenList.get(index).byteSize) {
				case 1:
					code = String.format("%02X", tmp).toUpperCase();
					break;
				case 2:
					code = String.format("%04X", tmp).toUpperCase();
					break;
				case 3:
					code = String.format("%06X", tmp).toUpperCase();
					break;
				case 4:
					code = String.format("%08X", tmp).toUpperCase();
					break;
				default : 
					code += String.format("%06X", tmp).toUpperCase();
					break;
				}
			}
			
		}
		
		tokenList.get(0).location += tokenList.get(index).byteSize; //because Header's location is always 0
		tokenList.get(index).objectCode = code;
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;
	char record;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		// initialize ???
		this.location = Assembler.locctr;
		this.label = "";
		this.operator = "";
		this.comment = "";
		this.operand = new String[1];
		this.operand[0] = "";
		parsing(line);
		
		this.objectCode = "";
		this.byteSize = 0;
		
		this.nixbpe = 0;
		this.record ='T';
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[]token  =line.split("\t");
		String[]tmp;
		for (int type = 0; type<token.length; type++) {
			if (token[type].isEmpty())
				continue;
			switch (type) {
			case 0: //label
				this.label = token[0];
				break;
			case 1: //operator
				this.operator = token[1];
				break;
			case 2://operand
				tmp = token[2].split(",");
				this.operand = new String[tmp.length];
				for (int i = 0; i<tmp.length; i++)
					this.operand[i] = tmp[i];
				break;
			case 3: //comment
				this.comment = token[3];
			}
		}
	}

	/**
	 * n,i,x,b,p,e flag를 설정한다.
	 * 
	 * 
	 * 사용 예 : setFlag(nFlag, 1) 또는 setFlag(TokenTable.nFlag, 1)
	 * 
	 * @param flag  : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		// ...
		
		if (value == 1)
			this.nixbpe |= flag;
		else {
			int digit = (int) (Math.log(flag)/Math.log(2));
			this.nixbpe &= ~(1 << digit);
		}
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다.
	 * 
	 * 사용 예 : getFlag(nFlag) 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */

	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
}

class Modification{
	int location;
	int length;
	char sign;
	String operand;
	public Modification (int location, int length, char sign, String operand) {
		this.location = location;
		this.length = length;
		this.sign = sign;
		this.operand = operand;
	}

}
