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
		RULE_defaultSubmodelSpec = 7, RULE_idBasedSubmodelSpec = 8, RULE_fullSubmodelElementSpec = 9, 
		RULE_defaultSubmodelElementSpec = 10, RULE_idShortPath = 11, RULE_idShortSeg = 12, 
		RULE_parameterSpec = 13, RULE_argumentSpec = 14, RULE_opVarSpec = 15, 
		RULE_valueLiteralSpec = 16, RULE_propertyValueLiteralSpec = 17, RULE_mlpPropertyValueLiteralSpec = 18, 
		RULE_fileValueLiteralSpec = 19, RULE_rangeValueLiteralSpec = 20, RULE_idOrString = 21;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "expr", "assignmentExpr", "fullInstanceSpec", "instanceSpec", 
			"fullSubmodelSpec", "submodelSpec", "defaultSubmodelSpec", "idBasedSubmodelSpec", 
			"fullSubmodelElementSpec", "defaultSubmodelElementSpec", "idShortPath", 
			"idShortSeg", "parameterSpec", "argumentSpec", "opVarSpec", "valueLiteralSpec", 
			"propertyValueLiteralSpec", "mlpPropertyValueLiteralSpec", "fileValueLiteralSpec", 
			"rangeValueLiteralSpec", "idOrString"
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
			setState(44);
			expr();
			setState(45);
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
		public FullSubmodelElementSpecContext fullSubmodelElementSpec() {
			return getRuleContext(FullSubmodelElementSpecContext.class,0);
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
			setState(51);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(47);
				valueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(48);
				fullSubmodelSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(49);
				fullSubmodelElementSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(50);
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
		public List<FullSubmodelElementSpecContext> fullSubmodelElementSpec() {
			return getRuleContexts(FullSubmodelElementSpecContext.class);
		}
		public FullSubmodelElementSpecContext fullSubmodelElementSpec(int i) {
			return getRuleContext(FullSubmodelElementSpecContext.class,i);
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
			setState(61);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(53);
				fullSubmodelElementSpec();
				setState(54);
				match(T__0);
				setState(55);
				valueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				fullSubmodelElementSpec();
				setState(58);
				match(T__0);
				setState(59);
				fullSubmodelElementSpec();
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
			setState(63);
			match(T__1);
			setState(64);
			match(T__2);
			setState(65);
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
			setState(67);
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
			setState(69);
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
			setState(73);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(71);
				defaultSubmodelSpec();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 2);
				{
				setState(72);
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
			setState(75);
			instanceSpec();
			setState(76);
			match(T__2);
			setState(79);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(77);
				match(T__3);
				setState(78);
				match(T__0);
				}
			}

			setState(81);
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
			setState(83);
			match(T__4);
			setState(84);
			match(T__2);
			setState(85);
			match(T__5);
			setState(86);
			match(T__0);
			setState(87);
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
	public static class FullSubmodelElementSpecContext extends ParserRuleContext {
		public DefaultSubmodelElementSpecContext defaultSubmodelElementSpec() {
			return getRuleContext(DefaultSubmodelElementSpecContext.class,0);
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
		public FullSubmodelElementSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullSubmodelElementSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitFullSubmodelElementSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullSubmodelElementSpecContext fullSubmodelElementSpec() throws RecognitionException {
		FullSubmodelElementSpecContext _localctx = new FullSubmodelElementSpecContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_fullSubmodelElementSpec);
		try {
			setState(99);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
			case ID:
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(89);
				defaultSubmodelElementSpec();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 2);
				{
				setState(90);
				match(T__6);
				setState(91);
				match(T__2);
				setState(92);
				parameterSpec();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 3);
				{
				setState(93);
				match(T__7);
				setState(94);
				match(T__2);
				setState(95);
				argumentSpec();
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 4);
				{
				setState(96);
				match(T__8);
				setState(97);
				match(T__2);
				setState(98);
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
	public static class DefaultSubmodelElementSpecContext extends ParserRuleContext {
		public SubmodelSpecContext submodelSpec() {
			return getRuleContext(SubmodelSpecContext.class,0);
		}
		public IdShortPathContext idShortPath() {
			return getRuleContext(IdShortPathContext.class,0);
		}
		public DefaultSubmodelElementSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultSubmodelElementSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitDefaultSubmodelElementSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultSubmodelElementSpecContext defaultSubmodelElementSpec() throws RecognitionException {
		DefaultSubmodelElementSpecContext _localctx = new DefaultSubmodelElementSpecContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_defaultSubmodelElementSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			submodelSpec();
			setState(102);
			match(T__2);
			setState(103);
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
			setState(105);
			idOrString();
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__9 || _la==T__10) {
				{
				{
				setState(106);
				idShortSeg();
				}
				}
				setState(111);
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
			setState(117);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__9:
				enterOuterAlt(_localctx, 1);
				{
				setState(112);
				match(T__9);
				setState(113);
				idOrString();
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 2);
				{
				setState(114);
				match(T__10);
				setState(115);
				match(INTEGER);
				setState(116);
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
		public InstanceSpecContext instanceSpec() {
			return getRuleContext(InstanceSpecContext.class,0);
		}
		public IdOrStringContext idOrString() {
			return getRuleContext(IdOrStringContext.class,0);
		}
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
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
			setState(119);
			instanceSpec();
			setState(120);
			match(T__2);
			setState(124);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				{
				setState(121);
				idOrString();
				}
				break;
			case INTEGER:
				{
				setState(122);
				match(INTEGER);
				}
				break;
			case T__12:
				{
				setState(123);
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
		enterRule(_localctx, 28, RULE_argumentSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			submodelSpec();
			setState(127);
			match(T__2);
			setState(128);
			_la = _input.LA(1);
			if ( !(_la==T__13 || _la==T__14) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(129);
			match(T__2);
			setState(133);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case STRING:
				{
				setState(130);
				idOrString();
				}
				break;
			case INTEGER:
				{
				setState(131);
				match(INTEGER);
				}
				break;
			case T__12:
				{
				setState(132);
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
		public DefaultSubmodelElementSpecContext defaultSubmodelElementSpec() {
			return getRuleContext(DefaultSubmodelElementSpecContext.class,0);
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
		enterRule(_localctx, 30, RULE_opVarSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			defaultSubmodelElementSpec();
			setState(136);
			match(T__2);
			setState(137);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 114688L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(138);
			match(T__2);
			{
			setState(139);
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
		enterRule(_localctx, 32, RULE_valueLiteralSpec);
		try {
			setState(145);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(141);
				propertyValueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				mlpPropertyValueLiteralSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(143);
				fileValueLiteralSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(144);
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
		enterRule(_localctx, 34, RULE_propertyValueLiteralSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
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
		enterRule(_localctx, 36, RULE_mlpPropertyValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			match(STRING);
			setState(150);
			match(T__16);
			setState(151);
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
		enterRule(_localctx, 38, RULE_fileValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			match(T__17);
			setState(154);
			match(T__2);
			setState(155);
			match(STRING);
			setState(156);
			match(T__18);
			setState(157);
			match(STRING);
			setState(158);
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
		enterRule(_localctx, 40, RULE_rangeValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(T__10);
			setState(161);
			propertyValueLiteralSpec();
			setState(162);
			match(T__20);
			setState(163);
			propertyValueLiteralSpec();
			setState(164);
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
		enterRule(_localctx, 42, RULE_idOrString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
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
		"\u0004\u0001\u001b\u00a9\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0003\u00014\b\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003"+
		"\u0002>\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0003"+
		"\u0006J\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007P\b\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\td\b\t\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0005\u000bl\b\u000b\n\u000b\f\u000bo\t\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\fv\b\f\u0001\r\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0003\r}\b\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0086\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0092\b\u0010"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0000\u0000\u0016\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*\u0000\u0004\u0001\u0000\u000e\u000f\u0001\u0000\u000e\u0010"+
		"\u0001\u0000\u0016\u001a\u0002\u0000\u0016\u0016\u001a\u001a\u00a4\u0000"+
		",\u0001\u0000\u0000\u0000\u00023\u0001\u0000\u0000\u0000\u0004=\u0001"+
		"\u0000\u0000\u0000\u0006?\u0001\u0000\u0000\u0000\bC\u0001\u0000\u0000"+
		"\u0000\nE\u0001\u0000\u0000\u0000\fI\u0001\u0000\u0000\u0000\u000eK\u0001"+
		"\u0000\u0000\u0000\u0010S\u0001\u0000\u0000\u0000\u0012c\u0001\u0000\u0000"+
		"\u0000\u0014e\u0001\u0000\u0000\u0000\u0016i\u0001\u0000\u0000\u0000\u0018"+
		"u\u0001\u0000\u0000\u0000\u001aw\u0001\u0000\u0000\u0000\u001c~\u0001"+
		"\u0000\u0000\u0000\u001e\u0087\u0001\u0000\u0000\u0000 \u0091\u0001\u0000"+
		"\u0000\u0000\"\u0093\u0001\u0000\u0000\u0000$\u0095\u0001\u0000\u0000"+
		"\u0000&\u0099\u0001\u0000\u0000\u0000(\u00a0\u0001\u0000\u0000\u0000*"+
		"\u00a6\u0001\u0000\u0000\u0000,-\u0003\u0002\u0001\u0000-.\u0005\u0000"+
		"\u0000\u0001.\u0001\u0001\u0000\u0000\u0000/4\u0003 \u0010\u000004\u0003"+
		"\n\u0005\u000014\u0003\u0012\t\u000024\u0003\u0004\u0002\u00003/\u0001"+
		"\u0000\u0000\u000030\u0001\u0000\u0000\u000031\u0001\u0000\u0000\u0000"+
		"32\u0001\u0000\u0000\u00004\u0003\u0001\u0000\u0000\u000056\u0003\u0012"+
		"\t\u000067\u0005\u0001\u0000\u000078\u0003 \u0010\u00008>\u0001\u0000"+
		"\u0000\u00009:\u0003\u0012\t\u0000:;\u0005\u0001\u0000\u0000;<\u0003\u0012"+
		"\t\u0000<>\u0001\u0000\u0000\u0000=5\u0001\u0000\u0000\u0000=9\u0001\u0000"+
		"\u0000\u0000>\u0005\u0001\u0000\u0000\u0000?@\u0005\u0002\u0000\u0000"+
		"@A\u0005\u0003\u0000\u0000AB\u0003\b\u0004\u0000B\u0007\u0001\u0000\u0000"+
		"\u0000CD\u0003*\u0015\u0000D\t\u0001\u0000\u0000\u0000EF\u0003\f\u0006"+
		"\u0000F\u000b\u0001\u0000\u0000\u0000GJ\u0003\u000e\u0007\u0000HJ\u0003"+
		"\u0010\b\u0000IG\u0001\u0000\u0000\u0000IH\u0001\u0000\u0000\u0000J\r"+
		"\u0001\u0000\u0000\u0000KL\u0003\b\u0004\u0000LO\u0005\u0003\u0000\u0000"+
		"MN\u0005\u0004\u0000\u0000NP\u0005\u0001\u0000\u0000OM\u0001\u0000\u0000"+
		"\u0000OP\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000QR\u0003*\u0015"+
		"\u0000R\u000f\u0001\u0000\u0000\u0000ST\u0005\u0005\u0000\u0000TU\u0005"+
		"\u0003\u0000\u0000UV\u0005\u0006\u0000\u0000VW\u0005\u0001\u0000\u0000"+
		"WX\u0003*\u0015\u0000X\u0011\u0001\u0000\u0000\u0000Yd\u0003\u0014\n\u0000"+
		"Z[\u0005\u0007\u0000\u0000[\\\u0005\u0003\u0000\u0000\\d\u0003\u001a\r"+
		"\u0000]^\u0005\b\u0000\u0000^_\u0005\u0003\u0000\u0000_d\u0003\u001c\u000e"+
		"\u0000`a\u0005\t\u0000\u0000ab\u0005\u0003\u0000\u0000bd\u0003\u001e\u000f"+
		"\u0000cY\u0001\u0000\u0000\u0000cZ\u0001\u0000\u0000\u0000c]\u0001\u0000"+
		"\u0000\u0000c`\u0001\u0000\u0000\u0000d\u0013\u0001\u0000\u0000\u0000"+
		"ef\u0003\f\u0006\u0000fg\u0005\u0003\u0000\u0000gh\u0003\u0016\u000b\u0000"+
		"h\u0015\u0001\u0000\u0000\u0000im\u0003*\u0015\u0000jl\u0003\u0018\f\u0000"+
		"kj\u0001\u0000\u0000\u0000lo\u0001\u0000\u0000\u0000mk\u0001\u0000\u0000"+
		"\u0000mn\u0001\u0000\u0000\u0000n\u0017\u0001\u0000\u0000\u0000om\u0001"+
		"\u0000\u0000\u0000pq\u0005\n\u0000\u0000qv\u0003*\u0015\u0000rs\u0005"+
		"\u000b\u0000\u0000st\u0005\u0017\u0000\u0000tv\u0005\f\u0000\u0000up\u0001"+
		"\u0000\u0000\u0000ur\u0001\u0000\u0000\u0000v\u0019\u0001\u0000\u0000"+
		"\u0000wx\u0003\b\u0004\u0000x|\u0005\u0003\u0000\u0000y}\u0003*\u0015"+
		"\u0000z}\u0005\u0017\u0000\u0000{}\u0005\r\u0000\u0000|y\u0001\u0000\u0000"+
		"\u0000|z\u0001\u0000\u0000\u0000|{\u0001\u0000\u0000\u0000}\u001b\u0001"+
		"\u0000\u0000\u0000~\u007f\u0003\f\u0006\u0000\u007f\u0080\u0005\u0003"+
		"\u0000\u0000\u0080\u0081\u0007\u0000\u0000\u0000\u0081\u0085\u0005\u0003"+
		"\u0000\u0000\u0082\u0086\u0003*\u0015\u0000\u0083\u0086\u0005\u0017\u0000"+
		"\u0000\u0084\u0086\u0005\r\u0000\u0000\u0085\u0082\u0001\u0000\u0000\u0000"+
		"\u0085\u0083\u0001\u0000\u0000\u0000\u0085\u0084\u0001\u0000\u0000\u0000"+
		"\u0086\u001d\u0001\u0000\u0000\u0000\u0087\u0088\u0003\u0014\n\u0000\u0088"+
		"\u0089\u0005\u0003\u0000\u0000\u0089\u008a\u0007\u0001\u0000\u0000\u008a"+
		"\u008b\u0005\u0003\u0000\u0000\u008b\u008c\u0005\u0017\u0000\u0000\u008c"+
		"\u001f\u0001\u0000\u0000\u0000\u008d\u0092\u0003\"\u0011\u0000\u008e\u0092"+
		"\u0003$\u0012\u0000\u008f\u0092\u0003&\u0013\u0000\u0090\u0092\u0003("+
		"\u0014\u0000\u0091\u008d\u0001\u0000\u0000\u0000\u0091\u008e\u0001\u0000"+
		"\u0000\u0000\u0091\u008f\u0001\u0000\u0000\u0000\u0091\u0090\u0001\u0000"+
		"\u0000\u0000\u0092!\u0001\u0000\u0000\u0000\u0093\u0094\u0007\u0002\u0000"+
		"\u0000\u0094#\u0001\u0000\u0000\u0000\u0095\u0096\u0005\u001a\u0000\u0000"+
		"\u0096\u0097\u0005\u0011\u0000\u0000\u0097\u0098\u0005\u0016\u0000\u0000"+
		"\u0098%\u0001\u0000\u0000\u0000\u0099\u009a\u0005\u0012\u0000\u0000\u009a"+
		"\u009b\u0005\u0003\u0000\u0000\u009b\u009c\u0005\u001a\u0000\u0000\u009c"+
		"\u009d\u0005\u0013\u0000\u0000\u009d\u009e\u0005\u001a\u0000\u0000\u009e"+
		"\u009f\u0005\u0014\u0000\u0000\u009f\'\u0001\u0000\u0000\u0000\u00a0\u00a1"+
		"\u0005\u000b\u0000\u0000\u00a1\u00a2\u0003\"\u0011\u0000\u00a2\u00a3\u0005"+
		"\u0015\u0000\u0000\u00a3\u00a4\u0003\"\u0011\u0000\u00a4\u00a5\u0005\f"+
		"\u0000\u0000\u00a5)\u0001\u0000\u0000\u0000\u00a6\u00a7\u0007\u0003\u0000"+
		"\u0000\u00a7+\u0001\u0000\u0000\u0000\n3=IOcmu|\u0085\u0091";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}