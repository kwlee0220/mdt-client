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
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, NULL=35, BOOLEAN=36, ISO8601_DURATION=37, 
		TIMESTAMP=38, ID=39, INTEGER=40, FLOAT=41, STRING=42, WS=43;
	public static final int
		RULE_start = 0, RULE_expr = 1, RULE_nullExpr = 2, RULE_assignmentExpr = 3, 
		RULE_fullInstanceSpec = 4, RULE_instanceSpec = 5, RULE_submodelSpec = 6, 
		RULE_defaultSubmodelSpec = 7, RULE_idBasedSubmodelSpec = 8, RULE_fullElementSpec = 9, 
		RULE_defaultElementSpec = 10, RULE_idShortPath = 11, RULE_idShortSeg = 12, 
		RULE_parameterSpec = 13, RULE_argumentSpec = 14, RULE_opVarSpec = 15, 
		RULE_timeseriesSpec = 16, RULE_tsRangeSpec = 17, RULE_tsRangeLastSpec = 18, 
		RULE_tsRangeAnchor = 19, RULE_durationValue = 20, RULE_tsRangeAbsoluteSpec = 21, 
		RULE_tsProjectionSpec = 22, RULE_valueLiteralSpec = 23, RULE_propertyValueLiteralSpec = 24, 
		RULE_mlpPropertyValueLiteralSpec = 25, RULE_fileValueLiteralSpec = 26, 
		RULE_rangeValueLiteralSpec = 27, RULE_durationLiteralSpec = 28, RULE_iso8601DurationLiteralSpec = 29, 
		RULE_idOrString = 30, RULE_keyword = 31;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "expr", "nullExpr", "assignmentExpr", "fullInstanceSpec", "instanceSpec", 
			"submodelSpec", "defaultSubmodelSpec", "idBasedSubmodelSpec", "fullElementSpec", 
			"defaultElementSpec", "idShortPath", "idShortSeg", "parameterSpec", "argumentSpec", 
			"opVarSpec", "timeseriesSpec", "tsRangeSpec", "tsRangeLastSpec", "tsRangeAnchor", 
			"durationValue", "tsRangeAbsoluteSpec", "tsProjectionSpec", "valueLiteralSpec", 
			"propertyValueLiteralSpec", "mlpPropertyValueLiteralSpec", "fileValueLiteralSpec", 
			"rangeValueLiteralSpec", "durationLiteralSpec", "iso8601DurationLiteralSpec", 
			"idOrString", "keyword"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "'mdt'", "':'", "'idShort'", "'submodel'", "'id'", "'param'", 
			"'oparg'", "'opvar'", "'timeseries'", "'.'", "'['", "']'", "'*'", "'in'", 
			"'out'", "'inout'", "'#'", "'|'", "'last'", "'@'", "'now'", "'latest'", 
			"'~'", "','", "'-'", "'file'", "'('", "')'", "'ms'", "'s'", "'m'", "'h'", 
			"'d'", "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "NULL", 
			"BOOLEAN", "ISO8601_DURATION", "TIMESTAMP", "ID", "INTEGER", "FLOAT", 
			"STRING", "WS"
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
			setState(64);
			expr();
			setState(65);
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
		public SubmodelSpecContext submodelSpec() {
			return getRuleContext(SubmodelSpecContext.class,0);
		}
		public FullElementSpecContext fullElementSpec() {
			return getRuleContext(FullElementSpecContext.class,0);
		}
		public AssignmentExprContext assignmentExpr() {
			return getRuleContext(AssignmentExprContext.class,0);
		}
		public NullExprContext nullExpr() {
			return getRuleContext(NullExprContext.class,0);
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
			setState(72);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				valueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(68);
				submodelSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(69);
				fullElementSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(70);
				assignmentExpr();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(71);
				nullExpr();
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
	public static class NullExprContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(MdtExprParser.NULL, 0); }
		public NullExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullExpr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitNullExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullExprContext nullExpr() throws RecognitionException {
		NullExprContext _localctx = new NullExprContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_nullExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			match(NULL);
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
		enterRule(_localctx, 6, RULE_assignmentExpr);
		try {
			setState(84);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(76);
				fullElementSpec();
				setState(77);
				match(T__0);
				setState(78);
				valueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(80);
				fullElementSpec();
				setState(81);
				match(T__0);
				setState(82);
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
		enterRule(_localctx, 8, RULE_fullInstanceSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			match(T__1);
			setState(87);
			match(T__2);
			setState(88);
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
		enterRule(_localctx, 10, RULE_instanceSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
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
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(92);
				defaultSubmodelSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(93);
				idBasedSubmodelSpec();
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			instanceSpec();
			setState(97);
			match(T__2);
			setState(100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(98);
				match(T__3);
				setState(99);
				match(T__0);
				}
				break;
			}
			setState(102);
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
			setState(104);
			match(T__4);
			setState(105);
			match(T__2);
			setState(106);
			match(T__5);
			setState(107);
			match(T__0);
			setState(108);
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
		public ParameterSpecContext parameterSpec() {
			return getRuleContext(ParameterSpecContext.class,0);
		}
		public ArgumentSpecContext argumentSpec() {
			return getRuleContext(ArgumentSpecContext.class,0);
		}
		public OpVarSpecContext opVarSpec() {
			return getRuleContext(OpVarSpecContext.class,0);
		}
		public TimeseriesSpecContext timeseriesSpec() {
			return getRuleContext(TimeseriesSpecContext.class,0);
		}
		public DefaultElementSpecContext defaultElementSpec() {
			return getRuleContext(DefaultElementSpecContext.class,0);
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
			setState(123);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				match(T__6);
				setState(111);
				match(T__2);
				setState(112);
				parameterSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(113);
				match(T__7);
				setState(114);
				match(T__2);
				setState(115);
				argumentSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(116);
				match(T__8);
				setState(117);
				match(T__2);
				setState(118);
				opVarSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(119);
				match(T__9);
				setState(120);
				match(T__2);
				setState(121);
				timeseriesSpec();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(122);
				defaultElementSpec();
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
			setState(125);
			submodelSpec();
			setState(126);
			match(T__2);
			setState(127);
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
			setState(129);
			idOrString();
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10 || _la==T__11) {
				{
				{
				setState(130);
				idShortSeg();
				}
				}
				setState(135);
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
			setState(141);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__10:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				match(T__10);
				setState(137);
				idOrString();
				}
				break;
			case T__11:
				enterOuterAlt(_localctx, 2);
				{
				setState(138);
				match(T__11);
				setState(139);
				match(INTEGER);
				setState(140);
				match(T__12);
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
		public IdShortPathContext idShortPath() {
			return getRuleContext(IdShortPathContext.class,0);
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
			setState(143);
			instanceSpec();
			setState(144);
			match(T__2);
			setState(148);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__14:
			case T__15:
			case T__16:
			case T__19:
			case T__21:
			case T__22:
			case T__26:
			case ID:
			case STRING:
				{
				setState(145);
				idShortPath();
				}
				break;
			case INTEGER:
				{
				setState(146);
				match(INTEGER);
				}
				break;
			case T__13:
				{
				setState(147);
				match(T__13);
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
			setState(150);
			submodelSpec();
			setState(151);
			match(T__2);
			setState(152);
			_la = _input.LA(1);
			if ( !(_la==T__14 || _la==T__15) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(153);
			match(T__2);
			setState(157);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__14:
			case T__15:
			case T__16:
			case T__19:
			case T__21:
			case T__22:
			case T__26:
			case ID:
			case STRING:
				{
				setState(154);
				idOrString();
				}
				break;
			case INTEGER:
				{
				setState(155);
				match(INTEGER);
				}
				break;
			case T__13:
				{
				setState(156);
				match(T__13);
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
		enterRule(_localctx, 30, RULE_opVarSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			defaultElementSpec();
			setState(160);
			match(T__2);
			setState(161);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 229376L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(162);
			match(T__2);
			{
			setState(163);
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
	public static class TimeseriesSpecContext extends ParserRuleContext {
		public SubmodelSpecContext submodelSpec() {
			return getRuleContext(SubmodelSpecContext.class,0);
		}
		public TsRangeSpecContext tsRangeSpec() {
			return getRuleContext(TsRangeSpecContext.class,0);
		}
		public TsProjectionSpecContext tsProjectionSpec() {
			return getRuleContext(TsProjectionSpecContext.class,0);
		}
		public TimeseriesSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeseriesSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitTimeseriesSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeseriesSpecContext timeseriesSpec() throws RecognitionException {
		TimeseriesSpecContext _localctx = new TimeseriesSpecContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_timeseriesSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			submodelSpec();
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__17) {
				{
				setState(166);
				match(T__17);
				setState(167);
				tsRangeSpec();
				}
			}

			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__18) {
				{
				setState(170);
				match(T__18);
				setState(171);
				tsProjectionSpec();
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
	public static class TsRangeSpecContext extends ParserRuleContext {
		public TsRangeLastSpecContext tsRangeLastSpec() {
			return getRuleContext(TsRangeLastSpecContext.class,0);
		}
		public TsRangeAbsoluteSpecContext tsRangeAbsoluteSpec() {
			return getRuleContext(TsRangeAbsoluteSpecContext.class,0);
		}
		public TsRangeSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tsRangeSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitTsRangeSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TsRangeSpecContext tsRangeSpec() throws RecognitionException {
		TsRangeSpecContext _localctx = new TsRangeSpecContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_tsRangeSpec);
		try {
			setState(176);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__19:
				enterOuterAlt(_localctx, 1);
				{
				setState(174);
				tsRangeLastSpec();
				}
				break;
			case T__23:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 2);
				{
				setState(175);
				tsRangeAbsoluteSpec();
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
	public static class TsRangeLastSpecContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public DurationValueContext durationValue() {
			return getRuleContext(DurationValueContext.class,0);
		}
		public TsRangeAnchorContext tsRangeAnchor() {
			return getRuleContext(TsRangeAnchorContext.class,0);
		}
		public TsRangeLastSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tsRangeLastSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitTsRangeLastSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TsRangeLastSpecContext tsRangeLastSpec() throws RecognitionException {
		TsRangeLastSpecContext _localctx = new TsRangeLastSpecContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_tsRangeLastSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			match(T__19);
			setState(179);
			match(T__0);
			setState(186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(180);
				match(INTEGER);
				}
				break;
			case 2:
				{
				setState(181);
				durationValue();
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__20) {
					{
					setState(182);
					match(T__20);
					setState(183);
					tsRangeAnchor();
					}
				}

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
	public static class TsRangeAnchorContext extends ParserRuleContext {
		public TsRangeAnchorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tsRangeAnchor; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitTsRangeAnchor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TsRangeAnchorContext tsRangeAnchor() throws RecognitionException {
		TsRangeAnchorContext _localctx = new TsRangeAnchorContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_tsRangeAnchor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			_la = _input.LA(1);
			if ( !(_la==T__21 || _la==T__22) ) {
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
	public static class DurationValueContext extends ParserRuleContext {
		public DurationLiteralSpecContext durationLiteralSpec() {
			return getRuleContext(DurationLiteralSpecContext.class,0);
		}
		public Iso8601DurationLiteralSpecContext iso8601DurationLiteralSpec() {
			return getRuleContext(Iso8601DurationLiteralSpecContext.class,0);
		}
		public DurationValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_durationValue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitDurationValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DurationValueContext durationValue() throws RecognitionException {
		DurationValueContext _localctx = new DurationValueContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_durationValue);
		try {
			setState(192);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
				enterOuterAlt(_localctx, 1);
				{
				setState(190);
				durationLiteralSpec();
				}
				break;
			case ISO8601_DURATION:
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
				iso8601DurationLiteralSpec();
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
	public static class TsRangeAbsoluteSpecContext extends ParserRuleContext {
		public List<TerminalNode> TIMESTAMP() { return getTokens(MdtExprParser.TIMESTAMP); }
		public TerminalNode TIMESTAMP(int i) {
			return getToken(MdtExprParser.TIMESTAMP, i);
		}
		public TsRangeAbsoluteSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tsRangeAbsoluteSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitTsRangeAbsoluteSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TsRangeAbsoluteSpecContext tsRangeAbsoluteSpec() throws RecognitionException {
		TsRangeAbsoluteSpecContext _localctx = new TsRangeAbsoluteSpecContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_tsRangeAbsoluteSpec);
		try {
			setState(201);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(194);
				match(TIMESTAMP);
				setState(195);
				match(T__23);
				setState(196);
				match(TIMESTAMP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(197);
				match(TIMESTAMP);
				setState(198);
				match(T__23);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(199);
				match(T__23);
				setState(200);
				match(TIMESTAMP);
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
	public static class TsProjectionSpecContext extends ParserRuleContext {
		public List<IdOrStringContext> idOrString() {
			return getRuleContexts(IdOrStringContext.class);
		}
		public IdOrStringContext idOrString(int i) {
			return getRuleContext(IdOrStringContext.class,i);
		}
		public TsProjectionSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tsProjectionSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitTsProjectionSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TsProjectionSpecContext tsProjectionSpec() throws RecognitionException {
		TsProjectionSpecContext _localctx = new TsProjectionSpecContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_tsProjectionSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			idOrString();
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__24) {
				{
				{
				setState(204);
				match(T__24);
				setState(205);
				idOrString();
				}
				}
				setState(210);
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
		enterRule(_localctx, 46, RULE_valueLiteralSpec);
		try {
			setState(215);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(211);
				propertyValueLiteralSpec();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(212);
				mlpPropertyValueLiteralSpec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(213);
				fileValueLiteralSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(214);
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
		enterRule(_localctx, 48, RULE_propertyValueLiteralSpec);
		int _la;
		try {
			setState(228);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(217);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(218);
				match(STRING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__25) {
					{
					setState(219);
					match(T__25);
					}
				}

				setState(222);
				match(INTEGER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(224);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__25) {
					{
					setState(223);
					match(T__25);
					}
				}

				setState(226);
				match(FLOAT);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(227);
				match(BOOLEAN);
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
		enterRule(_localctx, 50, RULE_mlpPropertyValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			match(STRING);
			setState(231);
			match(T__20);
			setState(232);
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
		enterRule(_localctx, 52, RULE_fileValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(T__26);
			setState(235);
			match(T__2);
			setState(236);
			match(STRING);
			setState(237);
			match(T__27);
			setState(238);
			match(STRING);
			setState(239);
			match(T__28);
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
		enterRule(_localctx, 54, RULE_rangeValueLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(241);
			match(T__11);
			setState(242);
			propertyValueLiteralSpec();
			setState(243);
			match(T__24);
			setState(244);
			propertyValueLiteralSpec();
			setState(245);
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
	public static class DurationLiteralSpecContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(MdtExprParser.INTEGER, 0); }
		public DurationLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_durationLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitDurationLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DurationLiteralSpecContext durationLiteralSpec() throws RecognitionException {
		DurationLiteralSpecContext _localctx = new DurationLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_durationLiteralSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(INTEGER);
			setState(248);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 33285996544L) != 0)) ) {
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
	public static class Iso8601DurationLiteralSpecContext extends ParserRuleContext {
		public TerminalNode ISO8601_DURATION() { return getToken(MdtExprParser.ISO8601_DURATION, 0); }
		public Iso8601DurationLiteralSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iso8601DurationLiteralSpec; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitIso8601DurationLiteralSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Iso8601DurationLiteralSpecContext iso8601DurationLiteralSpec() throws RecognitionException {
		Iso8601DurationLiteralSpecContext _localctx = new Iso8601DurationLiteralSpecContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_iso8601DurationLiteralSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250);
			match(ISO8601_DURATION);
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
		public KeywordContext keyword() {
			return getRuleContext(KeywordContext.class,0);
		}
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
		enterRule(_localctx, 60, RULE_idOrString);
		try {
			setState(255);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(252);
				match(ID);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
				match(STRING);
				}
				break;
			case T__1:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__14:
			case T__15:
			case T__16:
			case T__19:
			case T__21:
			case T__22:
			case T__26:
				enterOuterAlt(_localctx, 3);
				{
				setState(254);
				keyword();
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
	public static class KeywordContext extends ParserRuleContext {
		public KeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyword; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MdtExprVisitor ) return ((MdtExprVisitor<? extends T>)visitor).visitKeyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordContext keyword() throws RecognitionException {
		KeywordContext _localctx = new KeywordContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_keyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 148080628L) != 0)) ) {
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
		"\u0004\u0001+\u0104\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001I\b\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003U\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0003\u0006_\b\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007e\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t|\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u0084\b\u000b\n\u000b\f\u000b\u0087\t\u000b\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u008e\b\f\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0003\r\u0095\b\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u009e\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00a9\b\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u00ad\b\u0010\u0001\u0011\u0001\u0011\u0003\u0011"+
		"\u00b1\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0003\u0012\u00b9\b\u0012\u0003\u0012\u00bb\b\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u00c1\b\u0014\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u00ca\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0005"+
		"\u0016\u00cf\b\u0016\n\u0016\f\u0016\u00d2\t\u0016\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u00d8\b\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0003\u0018\u00dd\b\u0018\u0001\u0018\u0001\u0018\u0003\u0018"+
		"\u00e1\b\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u00e5\b\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0003\u001e\u0100\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0000"+
		"\u0000 \u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(*,.02468:<>\u0000\u0005\u0001\u0000\u000f\u0010"+
		"\u0001\u0000\u000f\u0011\u0001\u0000\u0016\u0017\u0001\u0000\u001e\"\u0006"+
		"\u0000\u0002\u0002\u0004\n\u000f\u0011\u0014\u0014\u0016\u0017\u001b\u001b"+
		"\u0108\u0000@\u0001\u0000\u0000\u0000\u0002H\u0001\u0000\u0000\u0000\u0004"+
		"J\u0001\u0000\u0000\u0000\u0006T\u0001\u0000\u0000\u0000\bV\u0001\u0000"+
		"\u0000\u0000\nZ\u0001\u0000\u0000\u0000\f^\u0001\u0000\u0000\u0000\u000e"+
		"`\u0001\u0000\u0000\u0000\u0010h\u0001\u0000\u0000\u0000\u0012{\u0001"+
		"\u0000\u0000\u0000\u0014}\u0001\u0000\u0000\u0000\u0016\u0081\u0001\u0000"+
		"\u0000\u0000\u0018\u008d\u0001\u0000\u0000\u0000\u001a\u008f\u0001\u0000"+
		"\u0000\u0000\u001c\u0096\u0001\u0000\u0000\u0000\u001e\u009f\u0001\u0000"+
		"\u0000\u0000 \u00a5\u0001\u0000\u0000\u0000\"\u00b0\u0001\u0000\u0000"+
		"\u0000$\u00b2\u0001\u0000\u0000\u0000&\u00bc\u0001\u0000\u0000\u0000("+
		"\u00c0\u0001\u0000\u0000\u0000*\u00c9\u0001\u0000\u0000\u0000,\u00cb\u0001"+
		"\u0000\u0000\u0000.\u00d7\u0001\u0000\u0000\u00000\u00e4\u0001\u0000\u0000"+
		"\u00002\u00e6\u0001\u0000\u0000\u00004\u00ea\u0001\u0000\u0000\u00006"+
		"\u00f1\u0001\u0000\u0000\u00008\u00f7\u0001\u0000\u0000\u0000:\u00fa\u0001"+
		"\u0000\u0000\u0000<\u00ff\u0001\u0000\u0000\u0000>\u0101\u0001\u0000\u0000"+
		"\u0000@A\u0003\u0002\u0001\u0000AB\u0005\u0000\u0000\u0001B\u0001\u0001"+
		"\u0000\u0000\u0000CI\u0003.\u0017\u0000DI\u0003\f\u0006\u0000EI\u0003"+
		"\u0012\t\u0000FI\u0003\u0006\u0003\u0000GI\u0003\u0004\u0002\u0000HC\u0001"+
		"\u0000\u0000\u0000HD\u0001\u0000\u0000\u0000HE\u0001\u0000\u0000\u0000"+
		"HF\u0001\u0000\u0000\u0000HG\u0001\u0000\u0000\u0000I\u0003\u0001\u0000"+
		"\u0000\u0000JK\u0005#\u0000\u0000K\u0005\u0001\u0000\u0000\u0000LM\u0003"+
		"\u0012\t\u0000MN\u0005\u0001\u0000\u0000NO\u0003.\u0017\u0000OU\u0001"+
		"\u0000\u0000\u0000PQ\u0003\u0012\t\u0000QR\u0005\u0001\u0000\u0000RS\u0003"+
		"\u0012\t\u0000SU\u0001\u0000\u0000\u0000TL\u0001\u0000\u0000\u0000TP\u0001"+
		"\u0000\u0000\u0000U\u0007\u0001\u0000\u0000\u0000VW\u0005\u0002\u0000"+
		"\u0000WX\u0005\u0003\u0000\u0000XY\u0003\n\u0005\u0000Y\t\u0001\u0000"+
		"\u0000\u0000Z[\u0003<\u001e\u0000[\u000b\u0001\u0000\u0000\u0000\\_\u0003"+
		"\u000e\u0007\u0000]_\u0003\u0010\b\u0000^\\\u0001\u0000\u0000\u0000^]"+
		"\u0001\u0000\u0000\u0000_\r\u0001\u0000\u0000\u0000`a\u0003\n\u0005\u0000"+
		"ad\u0005\u0003\u0000\u0000bc\u0005\u0004\u0000\u0000ce\u0005\u0001\u0000"+
		"\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000ef\u0001\u0000"+
		"\u0000\u0000fg\u0003<\u001e\u0000g\u000f\u0001\u0000\u0000\u0000hi\u0005"+
		"\u0005\u0000\u0000ij\u0005\u0003\u0000\u0000jk\u0005\u0006\u0000\u0000"+
		"kl\u0005\u0001\u0000\u0000lm\u0003<\u001e\u0000m\u0011\u0001\u0000\u0000"+
		"\u0000no\u0005\u0007\u0000\u0000op\u0005\u0003\u0000\u0000p|\u0003\u001a"+
		"\r\u0000qr\u0005\b\u0000\u0000rs\u0005\u0003\u0000\u0000s|\u0003\u001c"+
		"\u000e\u0000tu\u0005\t\u0000\u0000uv\u0005\u0003\u0000\u0000v|\u0003\u001e"+
		"\u000f\u0000wx\u0005\n\u0000\u0000xy\u0005\u0003\u0000\u0000y|\u0003 "+
		"\u0010\u0000z|\u0003\u0014\n\u0000{n\u0001\u0000\u0000\u0000{q\u0001\u0000"+
		"\u0000\u0000{t\u0001\u0000\u0000\u0000{w\u0001\u0000\u0000\u0000{z\u0001"+
		"\u0000\u0000\u0000|\u0013\u0001\u0000\u0000\u0000}~\u0003\f\u0006\u0000"+
		"~\u007f\u0005\u0003\u0000\u0000\u007f\u0080\u0003\u0016\u000b\u0000\u0080"+
		"\u0015\u0001\u0000\u0000\u0000\u0081\u0085\u0003<\u001e\u0000\u0082\u0084"+
		"\u0003\u0018\f\u0000\u0083\u0082\u0001\u0000\u0000\u0000\u0084\u0087\u0001"+
		"\u0000\u0000\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0085\u0086\u0001"+
		"\u0000\u0000\u0000\u0086\u0017\u0001\u0000\u0000\u0000\u0087\u0085\u0001"+
		"\u0000\u0000\u0000\u0088\u0089\u0005\u000b\u0000\u0000\u0089\u008e\u0003"+
		"<\u001e\u0000\u008a\u008b\u0005\f\u0000\u0000\u008b\u008c\u0005(\u0000"+
		"\u0000\u008c\u008e\u0005\r\u0000\u0000\u008d\u0088\u0001\u0000\u0000\u0000"+
		"\u008d\u008a\u0001\u0000\u0000\u0000\u008e\u0019\u0001\u0000\u0000\u0000"+
		"\u008f\u0090\u0003\n\u0005\u0000\u0090\u0094\u0005\u0003\u0000\u0000\u0091"+
		"\u0095\u0003\u0016\u000b\u0000\u0092\u0095\u0005(\u0000\u0000\u0093\u0095"+
		"\u0005\u000e\u0000\u0000\u0094\u0091\u0001\u0000\u0000\u0000\u0094\u0092"+
		"\u0001\u0000\u0000\u0000\u0094\u0093\u0001\u0000\u0000\u0000\u0095\u001b"+
		"\u0001\u0000\u0000\u0000\u0096\u0097\u0003\f\u0006\u0000\u0097\u0098\u0005"+
		"\u0003\u0000\u0000\u0098\u0099\u0007\u0000\u0000\u0000\u0099\u009d\u0005"+
		"\u0003\u0000\u0000\u009a\u009e\u0003<\u001e\u0000\u009b\u009e\u0005(\u0000"+
		"\u0000\u009c\u009e\u0005\u000e\u0000\u0000\u009d\u009a\u0001\u0000\u0000"+
		"\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009c\u0001\u0000\u0000"+
		"\u0000\u009e\u001d\u0001\u0000\u0000\u0000\u009f\u00a0\u0003\u0014\n\u0000"+
		"\u00a0\u00a1\u0005\u0003\u0000\u0000\u00a1\u00a2\u0007\u0001\u0000\u0000"+
		"\u00a2\u00a3\u0005\u0003\u0000\u0000\u00a3\u00a4\u0005(\u0000\u0000\u00a4"+
		"\u001f\u0001\u0000\u0000\u0000\u00a5\u00a8\u0003\f\u0006\u0000\u00a6\u00a7"+
		"\u0005\u0012\u0000\u0000\u00a7\u00a9\u0003\"\u0011\u0000\u00a8\u00a6\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u00ac\u0001"+
		"\u0000\u0000\u0000\u00aa\u00ab\u0005\u0013\u0000\u0000\u00ab\u00ad\u0003"+
		",\u0016\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000"+
		"\u0000\u0000\u00ad!\u0001\u0000\u0000\u0000\u00ae\u00b1\u0003$\u0012\u0000"+
		"\u00af\u00b1\u0003*\u0015\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000\u00b0"+
		"\u00af\u0001\u0000\u0000\u0000\u00b1#\u0001\u0000\u0000\u0000\u00b2\u00b3"+
		"\u0005\u0014\u0000\u0000\u00b3\u00ba\u0005\u0001\u0000\u0000\u00b4\u00bb"+
		"\u0005(\u0000\u0000\u00b5\u00b8\u0003(\u0014\u0000\u00b6\u00b7\u0005\u0015"+
		"\u0000\u0000\u00b7\u00b9\u0003&\u0013\u0000\u00b8\u00b6\u0001\u0000\u0000"+
		"\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000\u00b9\u00bb\u0001\u0000\u0000"+
		"\u0000\u00ba\u00b4\u0001\u0000\u0000\u0000\u00ba\u00b5\u0001\u0000\u0000"+
		"\u0000\u00bb%\u0001\u0000\u0000\u0000\u00bc\u00bd\u0007\u0002\u0000\u0000"+
		"\u00bd\'\u0001\u0000\u0000\u0000\u00be\u00c1\u00038\u001c\u0000\u00bf"+
		"\u00c1\u0003:\u001d\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c0\u00bf"+
		"\u0001\u0000\u0000\u0000\u00c1)\u0001\u0000\u0000\u0000\u00c2\u00c3\u0005"+
		"&\u0000\u0000\u00c3\u00c4\u0005\u0018\u0000\u0000\u00c4\u00ca\u0005&\u0000"+
		"\u0000\u00c5\u00c6\u0005&\u0000\u0000\u00c6\u00ca\u0005\u0018\u0000\u0000"+
		"\u00c7\u00c8\u0005\u0018\u0000\u0000\u00c8\u00ca\u0005&\u0000\u0000\u00c9"+
		"\u00c2\u0001\u0000\u0000\u0000\u00c9\u00c5\u0001\u0000\u0000\u0000\u00c9"+
		"\u00c7\u0001\u0000\u0000\u0000\u00ca+\u0001\u0000\u0000\u0000\u00cb\u00d0"+
		"\u0003<\u001e\u0000\u00cc\u00cd\u0005\u0019\u0000\u0000\u00cd\u00cf\u0003"+
		"<\u001e\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00cf\u00d2\u0001\u0000"+
		"\u0000\u0000\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001\u0000"+
		"\u0000\u0000\u00d1-\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000"+
		"\u0000\u00d3\u00d8\u00030\u0018\u0000\u00d4\u00d8\u00032\u0019\u0000\u00d5"+
		"\u00d8\u00034\u001a\u0000\u00d6\u00d8\u00036\u001b\u0000\u00d7\u00d3\u0001"+
		"\u0000\u0000\u0000\u00d7\u00d4\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001"+
		"\u0000\u0000\u0000\u00d7\u00d6\u0001\u0000\u0000\u0000\u00d8/\u0001\u0000"+
		"\u0000\u0000\u00d9\u00e5\u0005\'\u0000\u0000\u00da\u00e5\u0005*\u0000"+
		"\u0000\u00db\u00dd\u0005\u001a\u0000\u0000\u00dc\u00db\u0001\u0000\u0000"+
		"\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00de\u0001\u0000\u0000"+
		"\u0000\u00de\u00e5\u0005(\u0000\u0000\u00df\u00e1\u0005\u001a\u0000\u0000"+
		"\u00e0\u00df\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000\u0000\u0000"+
		"\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e5\u0005)\u0000\u0000\u00e3"+
		"\u00e5\u0005$\u0000\u0000\u00e4\u00d9\u0001\u0000\u0000\u0000\u00e4\u00da"+
		"\u0001\u0000\u0000\u0000\u00e4\u00dc\u0001\u0000\u0000\u0000\u00e4\u00e0"+
		"\u0001\u0000\u0000\u0000\u00e4\u00e3\u0001\u0000\u0000\u0000\u00e51\u0001"+
		"\u0000\u0000\u0000\u00e6\u00e7\u0005*\u0000\u0000\u00e7\u00e8\u0005\u0015"+
		"\u0000\u0000\u00e8\u00e9\u0005\'\u0000\u0000\u00e93\u0001\u0000\u0000"+
		"\u0000\u00ea\u00eb\u0005\u001b\u0000\u0000\u00eb\u00ec\u0005\u0003\u0000"+
		"\u0000\u00ec\u00ed\u0005*\u0000\u0000\u00ed\u00ee\u0005\u001c\u0000\u0000"+
		"\u00ee\u00ef\u0005*\u0000\u0000\u00ef\u00f0\u0005\u001d\u0000\u0000\u00f0"+
		"5\u0001\u0000\u0000\u0000\u00f1\u00f2\u0005\f\u0000\u0000\u00f2\u00f3"+
		"\u00030\u0018\u0000\u00f3\u00f4\u0005\u0019\u0000\u0000\u00f4\u00f5\u0003"+
		"0\u0018\u0000\u00f5\u00f6\u0005\r\u0000\u0000\u00f67\u0001\u0000\u0000"+
		"\u0000\u00f7\u00f8\u0005(\u0000\u0000\u00f8\u00f9\u0007\u0003\u0000\u0000"+
		"\u00f99\u0001\u0000\u0000\u0000\u00fa\u00fb\u0005%\u0000\u0000\u00fb;"+
		"\u0001\u0000\u0000\u0000\u00fc\u0100\u0005\'\u0000\u0000\u00fd\u0100\u0005"+
		"*\u0000\u0000\u00fe\u0100\u0003>\u001f\u0000\u00ff\u00fc\u0001\u0000\u0000"+
		"\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000\u00ff\u00fe\u0001\u0000\u0000"+
		"\u0000\u0100=\u0001\u0000\u0000\u0000\u0101\u0102\u0007\u0004\u0000\u0000"+
		"\u0102?\u0001\u0000\u0000\u0000\u0016HT^d{\u0085\u008d\u0094\u009d\u00a8"+
		"\u00ac\u00b0\u00b8\u00ba\u00c0\u00c9\u00d0\u00d7\u00dc\u00e0\u00e4\u00ff";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}