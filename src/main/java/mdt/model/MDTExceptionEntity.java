package mdt.model;

import java.lang.reflect.Constructor;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mdt.client.MDTClientException;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MDTExceptionEntity {
	@Nullable @JsonInclude(Include.NON_NULL) @JsonProperty("code")
	private String code;
	@Nullable @JsonInclude(Include.NON_NULL) @JsonProperty("text") 
	private String text;
	
	public static MDTExceptionEntity from(Exception e) {
		MDTExceptionEntity entity = new MDTExceptionEntity();
		entity.text = e.getMessage();
		entity.code = e.getClass().getName();
		
		return entity;
	}
	
	public MDTClientException toClientException() {
		if ( this.text != null ) {
			throw new MDTClientException("code=" + this.code + ", details=" + this.text);
		}
		else {
			throw new MDTClientException("code=" + this.code);
		}
	}
	
	public Throwable toException() {
		Class<? extends Throwable> cls = loadThrowableClass();
		try {
			if ( this.text != null ) {
				Constructor<? extends Throwable> ctor = getSingleStringContructor(cls);
				if ( ctor != null ) {
					return ctor.newInstance(this.text);
				}
				else {
					return new MDTClientException(this.text);
				}
			}
			else {
				Constructor<? extends Throwable> ctor = getNoArgContructor(cls);
				if ( ctor != null ) {
					return ctor.newInstance();
				}
				else {
					return new MDTClientException("code=" + this.code);
				}
			}
		}
		catch ( Exception e ) {
			if ( this.text != null ) {
				throw new MDTClientException("code=" + this.code + ", details=" + this.text);
			}
			else {
				throw new MDTClientException("code=" + this.code);
			}
		}
	}
	
	private Constructor<? extends Throwable>
	getNoArgContructor(Class<? extends Throwable> cls) {
		try {
			return cls.getDeclaredConstructor();
		}
		catch ( Throwable e ) {
			return null;
		}
	}
	
	private Constructor<? extends Throwable>
	getSingleStringContructor(Class<? extends Throwable> cls) {
		try {
			return cls.getDeclaredConstructor(String.class);
		}
		catch ( Throwable e ) {
			return null;
		}
	}
	
	private Class<? extends Throwable> loadThrowableClass() {
		try {
			return (Class<? extends Throwable>)Class.forName(this.code);
		}
		catch ( ClassNotFoundException e ) {
			return MDTClientException.class;
		}
	}
}
