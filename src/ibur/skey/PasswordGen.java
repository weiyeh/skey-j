package ibur.skey;

public class PasswordGen {
	public static String generatePassword(int pwlen, PwReq req) {
		char[] charset = req.getCharset();
		String res = "";
		do {
			char[] out = new char[pwlen];
			for(int i = 0; i < pwlen; i++) {
				out[i] = charset[Crypto.r.nextInt(charset.length)];
			}
			res = new String(out);
		} while(!req.validatePW(res));
		return res;
	}

	public static class PwReq {
		private static final String COMMON_SYMBOLS = "~!@#$%^&*()-_+=";
		private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
		private static final String UPPERCASE_LETTERS = LOWERCASE_LETTERS.toUpperCase();
		private static final String NUMBERS = "0123456789";

		private String usedSymbols;

		/**
		 * -1: none, 0: any number, 1: required
		 */
		public int lcase;
		public int ucase;
		public int numbers;
		public int symbols;

		public PwReq() {
			lcase = 0;
			ucase = 0;
			numbers = 0;
			symbols = 0;
			usedSymbols = COMMON_SYMBOLS;
		}

		public char[] getCharset() {
			String out = "";
			if(lcase >= 0) {
				out += LOWERCASE_LETTERS;
			}
			if(ucase >= 0) {
				out += UPPERCASE_LETTERS;
			}
			if(numbers >= 0) {
				out += NUMBERS;
			}
			if(symbols >= 0) {
				out += usedSymbols;
			}
			return out.toCharArray();
		}

		public boolean validatePW(String pw) {
			int lcaseNum = 0;
			int ucaseNum = 0;
			int numbersNum = 0;
			int symbolsNum = 0;
			for(int i = 0; i < pw.length(); i++) {
				char c = pw.charAt(i);

				// lcase check
				if(c >= 'a' && c <= 'z') {
					lcaseNum++;
				}

				// ucase check
				else if(c >= 'A' && c <= 'Z') {
					ucaseNum++;
				}

				// numbers check
				else if(c >= '0' && c <= '9') {
					numbersNum++;
				}

				// symbols check
				else if(usedSymbols.indexOf(c) >= 0) {
					symbolsNum++;
				}
			}

			boolean ret = true;

			if(lcaseNum < lcase || lcaseNum > 0 && lcase == -1) {
				ret = false;
			}

			if(ucaseNum < ucase || ucaseNum > 0 && ucase == -1) {
				ret = false;
			}

			if(numbersNum < numbers || numbersNum > 0 && numbers == -1) {
				ret = false;
			}

			if(symbolsNum < symbols || symbolsNum > 0 && symbols == -1) {
				ret = false;
			}

			return ret;
		}
	}
}
