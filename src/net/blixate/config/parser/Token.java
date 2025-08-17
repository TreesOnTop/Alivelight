package net.blixate.config.parser;

class Token {
  public TokenType t;
  
  String data;
  
  int index;
  
  public Token(TokenType t, String data, int start) {
    this.t = t;
    this.data = data;
    this.index = start;
  }
  
  public String toString() {
    return String.valueOf(this.t.name()) + "=\"" + this.data + "\" at " + this.index;
  }
  
  public String data() {
    return this.data;
  }
  
  public int index() {
    return this.index;
  }
}
