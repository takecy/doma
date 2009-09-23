/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.expr;

import static org.seasar.doma.internal.expr.ExpressionTokenType.*;
import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.nio.CharBuffer;

import org.seasar.doma.message.DomaMessageCode;


/**
 * @author taedium
 * 
 */
public class ExpressionTokenizer {

    protected final String expression;

    protected CharBuffer buf;

    protected CharBuffer duplicatedBuf;

    protected ExpressionTokenType type;

    protected String token;

    protected int position;

    protected boolean binOperationAvailable;

    public ExpressionTokenizer(String expression) {
        assertNotNull(expression);
        this.expression = expression;
        buf = CharBuffer.wrap(expression);
        duplicatedBuf = buf.duplicate();
        peek();
    }

    public ExpressionTokenType next() {
        switch (type) {
        case EOE:
            token = null;
            type = EOE;
            return EOE;
        default:
            ExpressionTokenType currentType = type;
            prepareToken();
            peek();
            return currentType;
        }
    }

    protected void prepareToken() {
        position = buf.position();
        duplicatedBuf.limit(position);
        token = duplicatedBuf.toString();
        duplicatedBuf = buf.duplicate();
    }

    public String getToken() {
        return token;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position, boolean binOperationAvailable) {
        this.position = position;
        this.binOperationAvailable = binOperationAvailable;
        duplicatedBuf.position(position);
        buf = duplicatedBuf.duplicate();
        peek();
    }

    protected void peek() {
        if (buf.hasRemaining()) {
            char c = buf.get();
            if (buf.hasRemaining()) {
                char c2 = buf.get();
                if (buf.hasRemaining()) {
                    char c3 = buf.get();
                    if (buf.hasRemaining()) {
                        char c4 = buf.get();
                        if (buf.hasRemaining()) {
                            char c5 = buf.get();
                            peekFiveChars(c, c2, c3, c4, c5);
                        } else {
                            peekFourChars(c, c2, c3, c4);
                        }
                    } else {
                        peekThreeChars(c, c2, c3);
                    }
                } else {
                    peekTwoChars(c, c2);
                }
            } else {
                peekOneChar(c);
            }
        } else {
            type = EOE;
        }
    }

    protected void peekFiveChars(char c, char c2, char c3, char c4, char c5) {
        if (c == 'f' && c2 == 'a' && c3 == 'l' && c4 == 's' && c5 == 'e') {
            if (isWordTerminated()) {
                type = FALSE_LITERAL;
                binOperationAvailable = true;
                return;
            }
        }
        buf.position(buf.position() - 1);
        peekFourChars(c, c2, c3, c4);
    }

    protected void peekFourChars(char c, char c2, char c3, char c4) {
        if (c == 'n' && c2 == 'u' && c3 == 'l' && c4 == 'l') {
            if (isWordTerminated()) {
                type = NULL_LITERAL;
                binOperationAvailable = true;
                return;
            }
        } else if (c == 't' && c2 == 'r' && c3 == 'u' && c4 == 'e') {
            if (isWordTerminated()) {
                type = TRUE_LITERAL;
                binOperationAvailable = true;
                return;
            }
        }
        buf.position(buf.position() - 1);
        peekThreeChars(c, c2, c3);
    }

    protected void peekThreeChars(char c, char c2, char c3) {
        if (c == 'n' && c2 == 'e' && c3 == 'w') {
            if (isWordTerminated()) {
                type = NEW_OPERATOR;
                return;
            }
        }
        buf.position(buf.position() - 1);
        peekTwoChars(c, c2);
    }

    protected void peekTwoChars(char c, char c2) {
        if (binOperationAvailable) {
            if (c == '&' && c2 == '&') {
                type = AND_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '|' && c2 == '|') {
                type = OR_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '=' && c2 == '=') {
                type = EQ_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '!' && c2 == '=') {
                type = NE_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '>' && c2 == '=') {
                type = GE_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '<' && c2 == '=') {
                type = LE_OPERATOR;
                binOperationAvailable = false;
                return;
            }
        }
        buf.position(buf.position() - 1);
        peekOneChar(c);
    }

    protected void peekOneChar(char c) {
        if (binOperationAvailable) {
            if (c == '>') {
                type = GT_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '<') {
                type = LT_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '+') {
                type = ADD_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '-') {
                type = SUBTRACT_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '*') {
                type = MULTIPLY_OPERATOR;
                binOperationAvailable = false;
                return;
            } else if (c == '/') {
                type = DIVIDE_OPERATOR;
                binOperationAvailable = false;
                return;
            }
        }
        if (Character.isWhitespace(c)) {
            type = WHITESPACE;
            return;
        } else if (c == ',') {
            type = COMMA_OPERATOR;
            return;
        } else if (c == '(') {
            type = OPENED_PARENS;
            return;
        } else if (c == ')') {
            type = CLOSED_PARENS;
            binOperationAvailable = true;
            return;
        } else if (c == '!') {
            type = NOT_OPERATOR;
            return;
        } else if (c == '\'') {
            type = CHAR_LITERAL;
            if (buf.hasRemaining()) {
                buf.get();
                if (buf.hasRemaining()) {
                    char c3 = buf.get();
                    if (c3 == '\'') {
                        binOperationAvailable = true;
                        return;
                    }
                }
            }
            throw new ExpressionException(DomaMessageCode.DOMA3016, expression, buf
                    .position());
        } else if (c == '"') {
            type = STRING_LITERAL;
            boolean closed = false;
            while (buf.hasRemaining()) {
                char c2 = buf.get();
                if (c2 == '"') {
                    if (buf.hasRemaining()) {
                        buf.mark();
                        char c3 = buf.get();
                        if (c3 != '"') {
                            buf.reset();
                            closed = true;
                            break;
                        }
                    } else {
                        closed = true;
                    }
                }
            }
            if (!closed) {
                throw new ExpressionException(DomaMessageCode.DOMA3004, expression,
                        buf.position());
            }
            binOperationAvailable = true;
        } else if ((c == '+' || c == '-')) {
            buf.mark();
            if (buf.hasRemaining()) {
                char c2 = buf.get();
                if (Character.isDigit(c2)) {
                    peekNumber();
                    return;
                }
                buf.reset();
            }
            type = ILLEGAL_NUMBER_LITERAL;
        } else if (Character.isDigit(c)) {
            peekNumber();
        } else if (Character.isJavaIdentifierStart(c)) {
            type = VARIABLE;
            binOperationAvailable = true;
            while (buf.hasRemaining()) {
                buf.mark();
                char c2 = buf.get();
                if (!Character.isJavaIdentifierPart(c2)) {
                    buf.reset();
                    break;
                }
            }
        } else if (c == '.') {
            type = FIELD_OPERATOR;
            binOperationAvailable = true;
            while (buf.hasRemaining()) {
                buf.mark();
                char c2 = buf.get();
                if (Character.isJavaIdentifierPart(c2)) {
                    while (buf.hasRemaining()) {
                        buf.mark();
                        char c3 = buf.get();
                        if (!Character.isJavaIdentifierPart(c3)) {
                            if (c3 == '(') {
                                type = METHOD_OPERATOR;
                                binOperationAvailable = false;
                            }
                            buf.reset();
                            break;
                        }
                    }
                } else {
                    buf.reset();
                    break;
                }
            }
        } else {
            type = OTHER;
        }
    }

    protected void peekNumber() {
        type = INT_LITERAL;
        boolean decimal = false;
        while (buf.hasRemaining()) {
            buf.mark();
            char c2 = buf.get();
            if (Character.isDigit(c2)) {
                continue;
            } else if (c2 == '.') {
                if (decimal) {
                    type = ILLEGAL_NUMBER_LITERAL;
                    return;
                }
                decimal = true;
                if (buf.hasRemaining()) {
                    char c3 = buf.get();
                    if (!Character.isDigit(c3)) {
                        type = ILLEGAL_NUMBER_LITERAL;
                        return;
                    }
                } else {
                    type = ILLEGAL_NUMBER_LITERAL;
                    return;
                }
            } else if (c2 == 'F') {
                type = FLOAT_LITERAL;
                break;
            } else if (c2 == 'D') {
                type = DOUBLE_LITERAL;
                break;
            } else if (c2 == 'L') {
                type = LONG_LITERAL;
                break;
            } else if (c2 == 'B') {
                type = BIGDECIMAL_LITERAL;
                break;
            } else {
                buf.reset();
                break;
            }
        }
        if (!isWordTerminated()) {
            type = ILLEGAL_NUMBER_LITERAL;
        }
        binOperationAvailable = true;
    }

    protected boolean isWordTerminated() {
        buf.mark();
        if (buf.hasRemaining()) {
            char c = buf.get();
            if (!Character.isJavaIdentifierPart(c)) {
                buf.reset();
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

}
