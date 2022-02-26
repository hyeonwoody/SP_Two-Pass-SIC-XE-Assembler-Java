import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Assembler: 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인루틴이다. 프로그램의 수행 작업은 다음과 같다.
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다.
 * 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다
 * 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1)
 * 
 * 4) 분석된 내용을바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
 * 
 * 
 */

public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<LabelTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간 */
	ArrayList<LabelTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * String codeList -> ArrayList<String>
	 */
	ArrayList<String> codeList;

	public static int locctr = 0;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);

		lineList = new ArrayList<String>();
		symtabList = new ArrayList<LabelTable>();
		literaltabList = new ArrayList<LabelTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");

		assembler.pass1();
		
		assembler.printSymbolTable("symtab_0000");
		assembler.printLiteralTable("literaltab_0000");
		assembler.pass2();
		assembler.printObjectCode("output_0000");

	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		BufferedReader file = null;
		try {
			file = new BufferedReader(new FileReader(new File(inputFile)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		try {
			while ((line = file.readLine()) != null) {
				lineList.add(line);
			}
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(instTable.getFormat("FLOAT"));

	}

	/**
	 * pass1 과정을 수행한다.
	 * 
	 * 1) 프로그램 소스를 스캔하여 토큰 단위로 분리한 뒤 토큰 테이블을 생성.
	 * 
	 * 2) symbol, literal 들을 SymbolTable, LiteralTable에 정리.
	 * 
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void pass1() {
		String[] token;
		int section = -1;

		int index = 0;
		locctr = 0;
		int location;
		String label;
		String operator;
		String tmp_operator;
		String[] operand;
		
		for (String line : lineList) {
			token = line.split("\t");

			if (token[0].equals("."))
				continue;
			if (token[1].equals("START") || token[1].equals("CSECT")) {
				section++;
				locctr = 0;
				index = 0;
				symtabList.add(new LabelTable());
				literaltabList.add(new LabelTable());
				TokenList.add(new TokenTable(symtabList.get(section), literaltabList.get(section), instTable));
			}
			TokenList.get(section).putToken(line); // tokenize

			location = TokenList.get(section).getToken(index).location;
			label = TokenList.get(section).getToken(index).label;

			operator = TokenList.get(section).getToken(index).operator;
			operand = new String[TokenList.get(section).getToken(index).operand.length];
			operand = TokenList.get(section).getToken(index).operand;

			/*
			 * System.out.println("location : "+location);
			 * System.out.println("label : "+label);
			 * System.out.println("operator : "+operator);
			 * System.out.println("operand :"+operand);
			 * System.out.println("comment : "+comment);
			 * System.out.println("nixbpe : "+nixbpe+"\n");
			 */
			if (label != "") // symtab
				symtabList.get(section).putName(label, locctr); // add to symbol

			if (operand[0] != "") // literaltab
				if (operand[0].charAt(0) == '=')
					if (literaltabList.get(section).search(operand[0]) == -1) {
						literaltabList.get(section).putName(operand[0], -1);
					}
			// System.out.println("THE : "+instTable.instMap.get("ADD").opcode);

			// calculating next location
			if (operator.charAt(0) == '+') {
				locctr++;
				TokenList.get(section).getToken(index).byteSize++;
				tmp_operator = operator.substring(1);
			} else {
				tmp_operator = operator;
			}
			if (instTable.instMap.get(tmp_operator) != null) {
				locctr += instTable.instMap.get(tmp_operator).format;
				TokenList.get(section).getToken(index).byteSize += instTable.instMap.get(tmp_operator).format;
				// instMap.get(TokenList.get(section).getToken(index).operator).opcode;
			} else if (operator.equals("RESW")) {
				locctr += 3 * Integer.parseInt(operand[0]);
				TokenList.get(section).getToken(index).byteSize += 3 * Integer.parseInt(operand[0]);
			} else if (operator.equals("RESB")) {
				locctr += Integer.parseInt(operand[0]);
				TokenList.get(section).getToken(index).byteSize += Integer.parseInt(operand[0]);
			} else if (operator.equals("WORD")) {
				locctr += 3;
				TokenList.get(section).getToken(index).byteSize += 3;
			} else if (operator.equals("BYTE")) {
				if (operand[0].charAt(0) == 'X') {
					locctr += (operand[0].length() - 3) / 2;
					TokenList.get(section).getToken(index).byteSize += (operand[0].length() - 3) / 2;
				} else {
					
					locctr += (operand[0].length() - 3);
					TokenList.get(section).getToken(index).byteSize += (operand[0].length() - 3);
				}
			} else if (operator.equals("EQU")) {
				if (operand[0].charAt(0) == '*') {}
				else {
					if (operand[0].contains("-")) {
						StringTokenizer tmp= new StringTokenizer(operand[0], "-");
						TokenList.get(section).getToken(index).location=symtabList.get(section).search(tmp.nextToken()) - symtabList.get(section).search(tmp.nextToken());
						symtabList.get(section).modifyName(label, TokenList.get(section).getToken(index).location);
					}
				}
				
			} else if ((operator.equals("LTORG") || operator.equals("END"))) {
				int more = 0;
				for (int i = 0; i<literaltabList.get(section).label.size(); i++) {
					if (literaltabList.get(section).locationList.get(i) == -1) {
						literaltabList.get(section).modifyName(literaltabList.get(section).label.get(i), location+more);
						if (literaltabList.get(section).label.get(i).charAt(1)=='X') {
							locctr += (literaltabList.get(section).label.get(i).length()-4)/2;
							more = (literaltabList.get(section).label.get(i).length()-4)/2;
							TokenList.get(section).getToken(index).byteSize += more;
						}
						else if (literaltabList.get(section).label.get(i).charAt(1)=='C') {
							locctr += (literaltabList.get(section).label.get(i).length()-4);
							more = (literaltabList.get(section).label.get(i).length()-4);
							TokenList.get(section).getToken(index).byteSize += more;
						}
						else {
							locctr += 3;
							more = (literaltabList.get(section).label.get(i).length()-1)*3;
							TokenList.get(section).getToken(index).byteSize += more;
						}
					}
				}
			}
			index++;
		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		PrintWriter file = null;
		//literaltabList.get(section).modifyName(literaltabList.get(section).label.get(i)
		try {
			file = new PrintWriter(new FileWriter(new File(fileName)));
			for (int i=0; i < symtabList.size(); i++) {
				for (int j=0; j < symtabList.get(i).label.size(); j++) {
					file.print(symtabList.get(i).label.get(j)+"\t");
					file.print(Integer.toHexString(symtabList.get(i).locationList.get(j)).toUpperCase()+"\n");
				}
				file.println("");
			}
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		PrintWriter file = null;
		//literaltabList.get(section).modifyName(literaltabList.get(section).label.get(i)
		try {
			file = new PrintWriter(new FileWriter(new File(fileName)));
			for (int i=0; i < literaltabList.size(); i++) {
				for (int j=0; j < literaltabList.get(i).label.size(); j++) {
					if (literaltabList.get(i).label.get(j).charAt(1)!='C' && literaltabList.get(i).label.get(j).charAt(1)!='X') {
						file.print(literaltabList.get(i).label.get(j).substring(1)+"\t");
						file.print(Integer.toHexString(literaltabList.get(i).locationList.get(j)).toUpperCase()+"\n");
					}
					else {
						file.print(literaltabList.get(i).label.get(j).substring(3, literaltabList.get(i).label.get(j).length()-1)+"\t");
						file.print(Integer.toHexString(literaltabList.get(i).locationList.get(j)).toUpperCase()+"\n");
					}
				}
			}
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * pass2 과정을 수행한다.
	 * 
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		locctr =0;
		//
		
		for (int section = 0; section <TokenList.size(); section++) {
			for (int i = 0; i < TokenList.get(section).tokenList.size(); i++) {
				TokenList.get(section).makeObjectCode(i);
				codeList.add(TokenList.get(section).getObjectCode(i));
			}
			System.out.print("\n");
		}
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		int cover =0;
		
		
		PrintWriter file = null;
		//literaltabList.get(section).modifyName(literaltabList.get(section).label.get(i)
		try {
			file = new PrintWriter(new FileWriter(new File(fileName)));
			for (int i = 0; i < TokenList.size(); i++) {
				
				for (int j=0; j<TokenList.get(i).tokenList.size(); j++) {
					if (TokenList.get(i).tokenList.get(j).objectCode!="") {
						switch (TokenList.get(i).tokenList.get(j).record) {
						case 'H':
							file.print(TokenList.get(i).tokenList.get(0).objectCode);
							file.println(String.format("%06X",TokenList.get(i).tokenList.get(0).location));
							break;
						case 'D':
						case 'R':
							file.print(TokenList.get(i).tokenList.get(j).objectCode+"\n");
	                        break;
						case 'T' :
							if (cover > 0) {
								cover--;
								file.print(TokenList.get(i).tokenList.get(j).objectCode);
							}
							else {
								int sum = TokenList.get(i).tokenList.get(j).byteSize;
								int k=j+1;
								int l=0;
								while (k<TokenList.get(i).tokenList.size()) {
									if (TokenList.get(i).tokenList.get(k).objectCode.equals("")) {//pass
										k++;
										l++;
										continue;
									}
									if (sum + TokenList.get(i).tokenList.get(k).byteSize > 30) { //pass
										k--;
										break;
									}
									if (TokenList.get(i).tokenList.get(k).location + TokenList.get(i).tokenList.get(k).byteSize - TokenList.get(i).tokenList.get(j).location > 30) {
										k--;
										break;
									}
									if (TokenList.get(i).tokenList.get(k).operator.equals("LTORG")) {
										k--;
										break;
									}
									sum += TokenList.get(i).tokenList.get(k).byteSize;
									k++;
									cover++;
								}
								file.print("T");
								file.print(String.format("%06X",TokenList.get(i).tokenList.get(j).location));
								file.print(String.format("%02X", sum));
								file.print(TokenList.get(i).tokenList.get(j).objectCode);
								//cover = k - j - l;
							}
							if (cover ==0)
								file.print("\n");
							break;
						}
					}
				}
				for (int k=0; k<TokenList.get(i).modifTab.size(); k++) {
					file.print("M");
					file.print(String.format(("%06X"), TokenList.get(i).modifTab.get(k).location));
					file.print(String.format(("%02X"), TokenList.get(i).modifTab.get(k).length));
					file.print(TokenList.get(i).modifTab.get(k).sign);
					file.print(TokenList.get(i).modifTab.get(k).operand);
					file.print("\n");
				}
				file.print("E");
				if (i == 0)
					file.print("000000");
				file.print("\n\n");
				
			}
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
