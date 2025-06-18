// Generated from MdtExpr.g4 by ANTLR 4.13.2

package mdt.model.expr;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MdtExprParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, ID=22, INTEGER=23, FLOAT=24, BOOLEAN=25, 
		STRING=26, WS=27;
	public static final int
		RULE_start = 0, RULE_expr = 1, RULE_assignmentExpr = 2, RULE_fullInstanceSpec = 3, 
		RULE_instanceSpec = 4, RULE_fullSubmodelSpec = 5, RULE_submodelSpec = 6, 
		RULE_defaultSubmodelSpec = 7, RULE_idBasedSubmodelSpec = 8, RULE_fullElementSpec = 9, 
		RULE_defaultElementSpec = 10, RULE_idShortPath = 11, RULE_idShortSeg = 12, 
		RULE_parameterSpec = 13, RULE_parameterPathSpec = 14, RULE_parameterAllSpec = 15, 
		RULE_argumentSpec = 16, RULE_opVarSpec = 17, RULE_valueLiteralSpec = 18, 
		RULE_propertyValueLiteralSpec = 19, RULE_mlpPropertyValueLiteralSpec = 20, 
		RULE_fileValueLiteralSpec = 21, RULE_rangeValueLiteralSpec = 22, RULE_idOrString = 23;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "expr", "assignmentExpr", "fullInstanceSpec", "instanceSpec", 
			"fullSubmodelSpec", "submodelSpec", "defaultSubmodelSpec", "idBasedSubmodelSpec", 
			"fullElementSpec", "defaultElementSpec", "idShortPath", "idShortSeg", 
			"parameterSpec", "parameterPathSpec", "parameterAllSpec", "argumentSpec", 
			"opVarSpec", "valueLiteralSpec", "propertyValueLiteralSpec", "mlpPropertyValueLiteralSpec", 
			"fileValueLiteralSpec", "rangeValueLiteralSpec", "idOrString"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "'mdt'", "':'", "'idShort'", "'submodel'", "'id'", "'param'", 
			"'oparg'", "'opvar'", "'.'", "'['", "']'", "'*'", "'in'", "'out'", "'inout'", 
			"'@'", "'file'", "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "ID", "INTEGER", 
			"FLOAT", "BOOLEAN", "STRING", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "MdtExpr.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MdtExprParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StartContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(MdtExprParser.EOF, 0); }
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			expr();
			setState(49);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public ValueLiteralSpecContext valueLiteralSpec() {
			return getRuleContext(ValueLiteralSpecContext.class,0);
		}
		public FullSubmodelSpecContext fullSubmodelSpec() {
			return getRuleContext(FullSubmodelSpecContext.class,0);
		}
		public FullElementSpecContext fullElementSpec() {
			return getRuleContext(FullElementSpecContext.class,0);
		}
		public AssignmentExprContext assignmentExpr() {
			return getRuleContext(AssignmentExprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expr);
		try {
			setState(55);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(51);
				valueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(52);
				fullSubmodelSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(53);
				fullElementSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(54);
				assignmentExpr();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentExprContext extends ParserRuleContext {
		public List<FullElementSpecContext> fullElementSpec() {
			return getRuleContexts(FullElementSpecContext.class);
		}
		public FullElementSpecContext fullElementSpec(int i) {
			return getRuleContext(FullElementSpecContext.class,i);
		}
		public ValueLiteralSpecContext valueLiteralSpec() {
			return getRuleContext(ValueLiteralSpecContext.class,0);
		}
		public AssignmentExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentExpr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitAssignmentExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentExprContext assignmentExpr() throws RecognitionException {
		AssignmentExprContext _localctx = new AssignmentExprContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_assignmentExpr);
		try {
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(57);
				fullElementSpec();
				setState(58);
				match(T__0);
				setState(59);
				valueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				fullElementSpec();
				setState(62);
				match(T__0);
				setState(63);
				fullElementSpec();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FullInstanceSpecContext extends ParserRuleContext {
		public InstanceSpecContext instanceSpec() {
			return getRuleContext(InstanceSpecContext.class,0);
		}
		public FullInstanceSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullInstanceSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitFullInstanceSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullInstanceSpecContext fullInstanceSpec() throws RecognitionException {
		FullInstanceSpecContext _localctx = new FullInstanceSpecContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_fullInstanceSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			match(T__1);
			setState(68);
			match(T__2);
			setState(69);
			instanceSpec();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InstanceSpecContext extends ParserRuleContext {
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public InstanceSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instanceSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitInstanceSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InstanceSpecContext instanceSpec() throws RecognitionException {
		InstanceSpecContext _localctx = new InstanceSpecContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_instanceSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			idOrString();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FullSubmodelSpecContext extends ParserRuleContext {
		public SubmodelSpecContext submodelSpec() {
			return getRuleContext(SubmodelSpecContext.class,0);
		}
		public FullSubmodelSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullSubmodelSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitFullSubmodelSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullSubmodelSpecContext fullSubmodelSpec() throws RecognitionException {
		FullSubmodelSpecContext _localctx = new FullSubmodelSpecContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fullSubmodelSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			submodelSpec();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SubmodelSpecContext extends ParserRuleContext {
		public DefaultSubmodelSpecContext defaultSubmodelSpec() {
			return getRuleContext(DefaultSubmodelSpecContext.class,0);
		}
		public IdBasedSubmodelSpecContext idBasedSubmodelSpec() {
			return getRuleContext(IdBasedSubmodelSpecContext.class,0);
		}
		public SubmodelSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_submodelSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitSubmodelSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubmodelSpecContext submodelSpec() throws RecognitionException {
		SubmodelSpecContext _localctx = new SubmodelSpecContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_submodelSpec);
		try {
			setState(77);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				defaultSubmodelSpec();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 2);
				{
				setState(76);
				idBasedSubmodelSpec();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultSubmodelSpecContext extends ParserRuleContext {
		public InstanceSpecContext instanceSpec() {
			return getRuleContext(InstanceSpecContext.class,0);
		}
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public DefaultSubmodelSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultSubmodelSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitDefaultSubmodelSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultSubmodelSpecContext defaultSubmodelSpec() throws RecognitionException {
		DefaultSubmodelSpecContext _localctx = new DefaultSubmodelSpecContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_defaultSubmodelSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			instanceSpec();
			setState(80);
			match(T__2);
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(81);
				match(T__3);
				setState(82);
				match(T__0);
				}
			}

			setState(85);
			idOrString();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdBasedSubmodelSpecContext extends ParserRuleContext {
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public IdBasedSubmodelSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idBasedSubmodelSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitIdBasedSubmodelSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdBasedSubmodelSpecContext idBasedSubmodelSpec() throws RecognitionException {
		IdBasedSubmodelSpecContext _localctx = new IdBasedSubmodelSpecContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_idBasedSubmodelSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(T__4);
			setState(88);
			match(T__2);
			setState(89);
			match(T__5);
			setState(90);
			match(T__0);
			setState(91);
			idOrString();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FullElementSpecContext extends ParserRuleContext {
		public DefaultElementSpecContext defaultElementSpec() {
			return getRuleContext(DefaultElementSpecContext.class,0);
		}
		public ParameterSpecContext parameterSpec() {
			return getRuleContext(ParameterSpecContext.class,0);
		}
		public ArgumentSpecContext argumentSpec() {
			return getRuleContext(ArgumentSpecContext.class,0);
		}
		public OpVarSpecContext opVarSpec() {
			return getRuleContext(OpVarSpecContext.class,0);
		}
		public FullElementSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullElementSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitFullElementSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullElementSpecContext fullElementSpec() throws RecognitionException {
		FullElementSpecContext _localctx = new FullElementSpecContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_fullElementSpec);
		try {
			setState(103);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
			case ID:
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				defaultElementSpec();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 2);
				{
				setState(94);
				match(T__6);
				setState(95);
				match(T__2);
				setState(96);
				parameterSpec();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 3);
				{
				setState(97);
				match(T__7);
				setState(98);
				match(T__2);
				setState(99);
				argumentSpec();
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 4);
				{
				setState(100);
				match(T__8);
				setState(101);
				match(T__2);
				setState(102);
				opVarSpec();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultElementSpecContext extends ParserRuleContext {
		public SubmodelSpecContext submodelSpec() {
			return getRuleContext(SubmodelSpecContext.class,0);
		}
		public IdShortPathContext idShortPath() {
			return getRuleContext(IdShortPathContext.class,0);
		}
		public DefaultElementSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultElementSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitDefaultElementSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultElementSpecContext defaultElementSpec() throws RecognitionException {
		DefaultElementSpecContext _localctx = new DefaultElementSpecContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_defaultElementSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			submodelSpec();
			setState(106);
			match(T__2);
			setState(107);
			idShortPath();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdShortPathContext extends ParserRuleContext {
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public List<IdShortSegContext> idShortSeg() {
			return getRuleContexts(IdShortSegContext.class);
		}
		public IdShortSegContext idShortSeg(int i) {
			return getRuleContext(IdShortSegContext.class,i);
		}
		public IdShortPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idShortPath; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitIdShortPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdShortPathContext idShortPath() throws RecognitionException {
		IdShortPathContext _localctx = new IdShortPathContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_idShortPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			idOrString();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__9 || _la==T__10) {
				{
				{
				setState(110);
				idShortSeg();
				}
				}
				setState(115);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdShortSegContext extends ParserRuleContext {
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public IdShortSegContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idShortSeg; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitIdShortSeg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdShortSegContext idShortSeg() throws RecognitionException {
		IdShortSegContext _localctx = new IdShortSegContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_idShortSeg);
		try {
			setState(121);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__9:
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				match(T__9);
				setState(117);
				idOrString();
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				match(T__10);
				setState(119);
				match(INTEGER);
				setState(120);
				match(T__11);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterSpecContext extends ParserRuleContext {
		public ParameterPathSpecContext parameterPathSpec() {
			return getRuleContext(ParameterPathSpecContext.class,0);
		}
		public ParameterAllSpecContext parameterAllSpec() {
			return getRuleContext(ParameterAllSpecContext.class,0);
		}
		public ParameterSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitParameterSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterSpecContext parameterSpec() throws RecognitionException {
		ParameterSpecContext _localctx = new ParameterSpecContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_parameterSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(123);
				parameterPathSpec();
				}
				break;
			case 2:
				{
				setState(124);
				parameterAllSpec();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterPathSpecContext extends ParserRuleContext {
		public InstanceSpecContext instanceSpec() {
			return getRuleContext(InstanceSpecContext.class,0);
		}
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public IdShortPathContext idShortPath() {
			return getRuleContext(IdShortPathContext.class,0);
		}
		public ParameterPathSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterPathSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitParameterPathSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterPathSpecContext parameterPathSpec() throws RecognitionException {
		ParameterPathSpecContext _localctx = new ParameterPathSpecContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_parameterPathSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			instanceSpec();
			setState(128);
			match(T__2);
			setState(131);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				{
				setState(129);
				idOrString();
				}
				break;
			case INTEGER:
				{
				setState(130);
				match(INTEGER);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(133);
				match(T__2);
				setState(134);
				idShortPath();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterAllSpecContext extends ParserRuleContext {
		public InstanceSpecContext instanceSpec() {
			return getRuleContext(InstanceSpecContext.class,0);
		}
		public ParameterAllSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterAllSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitParameterAllSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterAllSpecContext parameterAllSpec() throws RecognitionException {
		ParameterAllSpecContext _localctx = new ParameterAllSpecContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_parameterAllSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			instanceSpec();
			setState(138);
			match(T__2);
			setState(139);
			match(T__12);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentSpecContext extends ParserRuleContext {
		public SubmodelSpecContext submodelSpec() {
			return getRuleContext(SubmodelSpecContext.class,0);
		}
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public ArgumentSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitArgumentSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentSpecContext argumentSpec() throws RecognitionException {
		ArgumentSpecContext _localctx = new ArgumentSpecContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_argumentSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			submodelSpec();
			setState(142);
			match(T__2);
			setState(143);
			_la = _input.LA(1);
			if ( !(_la==T__13 || _la==T__14) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(144);
			match(T__2);
			setState(148);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				{
				setState(145);
				idOrString();
				}
				break;
			case INTEGER:
				{
				setState(146);
				match(INTEGER);
				}
				break;
			case T__12:
				{
				setState(147);
				match(T__12);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OpVarSpecContext extends ParserRuleContext {
		public DefaultElementSpecContext defaultElementSpec() {
			return getRuleContext(DefaultElementSpecContext.class,0);
		}
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public OpVarSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_opVarSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitOpVarSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpVarSpecContext opVarSpec() throws RecognitionException {
		OpVarSpecContext _localctx = new OpVarSpecContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_opVarSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			defaultElementSpec();
			setState(151);
			match(T__2);
			setState(152);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 114688L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(153);
			match(T__2);
			{
			setState(154);
			match(INTEGER);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueLiteralSpecContext extends ParserRuleContext {
		public PropertyValueLiteralSpecContext propertyValueLiteralSpec() {
			return getRuleContext(PropertyValueLiteralSpecContext.class,0);
		}
		public MlpPropertyValueLiteralSpecContext mlpPropertyValueLiteralSpec() {
			return getRuleContext(MlpPropertyValueLiteralSpecContext.class,0);
		}
		public FileValueLiteralSpecContext fileValueLiteralSpec() {
			return getRuleContext(FileValueLiteralSpecContext.class,0);
		}
		public RangeValueLiteralSpecContext rangeValueLiteralSpec() {
			return getRuleContext(RangeValueLiteralSpecContext.class,0);
		}
		public ValueLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitValueLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueLiteralSpecContext valueLiteralSpec() throws RecognitionException {
		ValueLiteralSpecContext _localctx = new ValueLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_valueLiteralSpec);
		try {
			setState(160);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(156);
				propertyValueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(157);
				mlpPropertyValueLiteralSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(158);
				fileValueLiteralSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(159);
				rangeValueLiteralSpec();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PropertyValueLiteralSpecContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MdtExprParser.ID, 0); }
		public TerminalNode STRING() { return getToken(MdtExprParser.STRING, 0); }
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public TerminalNode FLOAT() { return getToken(MdtExprParser.FLOAT, 0); }
		public TerminalNode BOOLEAN() { return getToken(MdtExprParser.BOOLEAN, 0); }
		public PropertyValueLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValueLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitPropertyValueLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyValueLiteralSpecContext propertyValueLiteralSpec() throws RecognitionException {
		PropertyValueLiteralSpecContext _localctx = new PropertyValueLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_propertyValueLiteralSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 130023424L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MlpPropertyValueLiteralSpecContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(MdtExprParser.STRING, 0); }
		public TerminalNode ID() { return getToken(MdtExprParser.ID, 0); }
		public MlpPropertyValueLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlpPropertyValueLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitMlpPropertyValueLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MlpPropertyValueLiteralSpecContext mlpPropertyValueLiteralSpec() throws RecognitionException {
		MlpPropertyValueLiteralSpecContext _localctx = new MlpPropertyValueLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_mlpPropertyValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			match(STRING);
			setState(165);
			match(T__16);
			setState(166);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileValueLiteralSpecContext extends ParserRuleContext {
		public List<TerminalNode> STRING() { return getTokens(MdtExprParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(MdtExprParser.STRING, i);
		}
		public FileValueLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fileValueLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitFileValueLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileValueLiteralSpecContext fileValueLiteralSpec() throws RecognitionException {
		FileValueLiteralSpecContext _localctx = new FileValueLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_fileValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			match(T__17);
			setState(169);
			match(T__2);
			setState(170);
			match(STRING);
			setState(171);
			match(T__18);
			setState(172);
			match(STRING);
			setState(173);
			match(T__19);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RangeValueLiteralSpecContext extends ParserRuleContext {
		public List<PropertyValueLiteralSpecContext> propertyValueLiteralSpec() {
			return getRuleContexts(PropertyValueLiteralSpecContext.class);
		}
		public PropertyValueLiteralSpecContext propertyValueLiteralSpec(int i) {
			return getRuleContext(PropertyValueLiteralSpecContext.class,i);
		}
		public RangeValueLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeValueLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitRangeValueLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeValueLiteralSpecContext rangeValueLiteralSpec() throws RecognitionException {
		RangeValueLiteralSpecContext _localctx = new RangeValueLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_rangeValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(T__10);
			setState(176);
			propertyValueLiteralSpec();
			setState(177);
			match(T__20);
			setState(178);
			propertyValueLiteralSpec();
			setState(179);
			match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdOrStringContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(MdtExprParser.ID, 0); }
		public TerminalNode STRING() { return getToken(MdtExprParser.STRING, 0); }
		public IdOrStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idOrString; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitIdOrString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdOrStringContext idOrString() throws RecognitionException {
		IdOrStringContext _localctx = new IdOrStringContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_idOrString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			_la = _input.LA(1);
			if ( !(_la==ID || _la==STRING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u001b\u00b8\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u00018\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002B\b\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0003\u0006N\b\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007T\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\th\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0005\u000bp\b\u000b\n\u000b\f\u000bs\t\u000b\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0003\fz\b\f\u0001\r\u0001\r\u0003\r~\b\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0084\b\u000e\u0001\u000e"+
		"\u0001\u000e\u0003\u000e\u0088\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0003\u0010\u0095\b\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u00a1\b\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0000\u0000\u0018\u0000\u0002\u0004\u0006\b\n"+
		"\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.\u0000"+
		"\u0004\u0001\u0000\u000e\u000f\u0001\u0000\u000e\u0010\u0001\u0000\u0016"+
		"\u001a\u0002\u0000\u0016\u0016\u001a\u001a\u00b2\u00000\u0001\u0000\u0000"+
		"\u0000\u00027\u0001\u0000\u0000\u0000\u0004A\u0001\u0000\u0000\u0000\u0006"+
		"C\u0001\u0000\u0000\u0000\bG\u0001\u0000\u0000\u0000\nI\u0001\u0000\u0000"+
		"\u0000\fM\u0001\u0000\u0000\u0000\u000eO\u0001\u0000\u0000\u0000\u0010"+
		"W\u0001\u0000\u0000\u0000\u0012g\u0001\u0000\u0000\u0000\u0014i\u0001"+
		"\u0000\u0000\u0000\u0016m\u0001\u0000\u0000\u0000\u0018y\u0001\u0000\u0000"+
		"\u0000\u001a}\u0001\u0000\u0000\u0000\u001c\u007f\u0001\u0000\u0000\u0000"+
		"\u001e\u0089\u0001\u0000\u0000\u0000 \u008d\u0001\u0000\u0000\u0000\""+
		"\u0096\u0001\u0000\u0000\u0000$\u00a0\u0001\u0000\u0000\u0000&\u00a2\u0001"+
		"\u0000\u0000\u0000(\u00a4\u0001\u0000\u0000\u0000*\u00a8\u0001\u0000\u0000"+
		"\u0000,\u00af\u0001\u0000\u0000\u0000.\u00b5\u0001\u0000\u0000\u00000"+
		"1\u0003\u0002\u0001\u000012\u0005\u0000\u0000\u00012\u0001\u0001\u0000"+
		"\u0000\u000038\u0003$\u0012\u000048\u0003\n\u0005\u000058\u0003\u0012"+
		"\t\u000068\u0003\u0004\u0002\u000073\u0001\u0000\u0000\u000074\u0001\u0000"+
		"\u0000\u000075\u0001\u0000\u0000\u000076\u0001\u0000\u0000\u00008\u0003"+
		"\u0001\u0000\u0000\u00009:\u0003\u0012\t\u0000:;\u0005\u0001\u0000\u0000"+
		";<\u0003$\u0012\u0000<B\u0001\u0000\u0000\u0000=>\u0003\u0012\t\u0000"+
		">?\u0005\u0001\u0000\u0000?@\u0003\u0012\t\u0000@B\u0001\u0000\u0000\u0000"+
		"A9\u0001\u0000\u0000\u0000A=\u0001\u0000\u0000\u0000B\u0005\u0001\u0000"+
		"\u0000\u0000CD\u0005\u0002\u0000\u0000DE\u0005\u0003\u0000\u0000EF\u0003"+
		"\b\u0004\u0000F\u0007\u0001\u0000\u0000\u0000GH\u0003.\u0017\u0000H\t"+
		"\u0001\u0000\u0000\u0000IJ\u0003\f\u0006\u0000J\u000b\u0001\u0000\u0000"+
		"\u0000KN\u0003\u000e\u0007\u0000LN\u0003\u0010\b\u0000MK\u0001\u0000\u0000"+
		"\u0000ML\u0001\u0000\u0000\u0000N\r\u0001\u0000\u0000\u0000OP\u0003\b"+
		"\u0004\u0000PS\u0005\u0003\u0000\u0000QR\u0005\u0004\u0000\u0000RT\u0005"+
		"\u0001\u0000\u0000SQ\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000"+
		"TU\u0001\u0000\u0000\u0000UV\u0003.\u0017\u0000V\u000f\u0001\u0000\u0000"+
		"\u0000WX\u0005\u0005\u0000\u0000XY\u0005\u0003\u0000\u0000YZ\u0005\u0006"+
		"\u0000\u0000Z[\u0005\u0001\u0000\u0000[\\\u0003.\u0017\u0000\\\u0011\u0001"+
		"\u0000\u0000\u0000]h\u0003\u0014\n\u0000^_\u0005\u0007\u0000\u0000_`\u0005"+
		"\u0003\u0000\u0000`h\u0003\u001a\r\u0000ab\u0005\b\u0000\u0000bc\u0005"+
		"\u0003\u0000\u0000ch\u0003 \u0010\u0000de\u0005\t\u0000\u0000ef\u0005"+
		"\u0003\u0000\u0000fh\u0003\"\u0011\u0000g]\u0001\u0000\u0000\u0000g^\u0001"+
		"\u0000\u0000\u0000ga\u0001\u0000\u0000\u0000gd\u0001\u0000\u0000\u0000"+
		"h\u0013\u0001\u0000\u0000\u0000ij\u0003\f\u0006\u0000jk\u0005\u0003\u0000"+
		"\u0000kl\u0003\u0016\u000b\u0000l\u0015\u0001\u0000\u0000\u0000mq\u0003"+
		".\u0017\u0000np\u0003\u0018\f\u0000on\u0001\u0000\u0000\u0000ps\u0001"+
		"\u0000\u0000\u0000qo\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000"+
		"r\u0017\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000tu\u0005\n\u0000"+
		"\u0000uz\u0003.\u0017\u0000vw\u0005\u000b\u0000\u0000wx\u0005\u0017\u0000"+
		"\u0000xz\u0005\f\u0000\u0000yt\u0001\u0000\u0000\u0000yv\u0001\u0000\u0000"+
		"\u0000z\u0019\u0001\u0000\u0000\u0000{~\u0003\u001c\u000e\u0000|~\u0003"+
		"\u001e\u000f\u0000}{\u0001\u0000\u0000\u0000}|\u0001\u0000\u0000\u0000"+
		"~\u001b\u0001\u0000\u0000\u0000\u007f\u0080\u0003\b\u0004\u0000\u0080"+
		"\u0083\u0005\u0003\u0000\u0000\u0081\u0084\u0003.\u0017\u0000\u0082\u0084"+
		"\u0005\u0017\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0083\u0082"+
		"\u0001\u0000\u0000\u0000\u0084\u0087\u0001\u0000\u0000\u0000\u0085\u0086"+
		"\u0005\u0003\u0000\u0000\u0086\u0088\u0003\u0016\u000b\u0000\u0087\u0085"+
		"\u0001\u0000\u0000\u0000\u0087\u0088\u0001\u0000\u0000\u0000\u0088\u001d"+
		"\u0001\u0000\u0000\u0000\u0089\u008a\u0003\b\u0004\u0000\u008a\u008b\u0005"+
		"\u0003\u0000\u0000\u008b\u008c\u0005\r\u0000\u0000\u008c\u001f\u0001\u0000"+
		"\u0000\u0000\u008d\u008e\u0003\f\u0006\u0000\u008e\u008f\u0005\u0003\u0000"+
		"\u0000\u008f\u0090\u0007\u0000\u0000\u0000\u0090\u0094\u0005\u0003\u0000"+
		"\u0000\u0091\u0095\u0003.\u0017\u0000\u0092\u0095\u0005\u0017\u0000\u0000"+
		"\u0093\u0095\u0005\r\u0000\u0000\u0094\u0091\u0001\u0000\u0000\u0000\u0094"+
		"\u0092\u0001\u0000\u0000\u0000\u0094\u0093\u0001\u0000\u0000\u0000\u0095"+
		"!\u0001\u0000\u0000\u0000\u0096\u0097\u0003\u0014\n\u0000\u0097\u0098"+
		"\u0005\u0003\u0000\u0000\u0098\u0099\u0007\u0001\u0000\u0000\u0099\u009a"+
		"\u0005\u0003\u0000\u0000\u009a\u009b\u0005\u0017\u0000\u0000\u009b#\u0001"+
		"\u0000\u0000\u0000\u009c\u00a1\u0003&\u0013\u0000\u009d\u00a1\u0003(\u0014"+
		"\u0000\u009e\u00a1\u0003*\u0015\u0000\u009f\u00a1\u0003,\u0016\u0000\u00a0"+
		"\u009c\u0001\u0000\u0000\u0000\u00a0\u009d\u0001\u0000\u0000\u0000\u00a0"+
		"\u009e\u0001\u0000\u0000\u0000\u00a0\u009f\u0001\u0000\u0000\u0000\u00a1"+
		"%\u0001\u0000\u0000\u0000\u00a2\u00a3\u0007\u0002\u0000\u0000\u00a3\'"+
		"\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005\u001a\u0000\u0000\u00a5\u00a6"+
		"\u0005\u0011\u0000\u0000\u00a6\u00a7\u0005\u0016\u0000\u0000\u00a7)\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a9\u0005\u0012\u0000\u0000\u00a9\u00aa\u0005"+
		"\u0003\u0000\u0000\u00aa\u00ab\u0005\u001a\u0000\u0000\u00ab\u00ac\u0005"+
		"\u0013\u0000\u0000\u00ac\u00ad\u0005\u001a\u0000\u0000\u00ad\u00ae\u0005"+
		"\u0014\u0000\u0000\u00ae+\u0001\u0000\u0000\u0000\u00af\u00b0\u0005\u000b"+
		"\u0000\u0000\u00b0\u00b1\u0003&\u0013\u0000\u00b1\u00b2\u0005\u0015\u0000"+
		"\u0000\u00b2\u00b3\u0003&\u0013\u0000\u00b3\u00b4\u0005\f\u0000\u0000"+
		"\u00b4-\u0001\u0000\u0000\u0000\u00b5\u00b6\u0007\u0003\u0000\u0000\u00b6"+
		"/\u0001\u0000\u0000\u0000\f7AMSgqy}\u0083\u0087\u0094\u00a0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}