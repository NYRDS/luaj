/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.lib.jse;


import java.lang.reflect.Method;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * Subclass of {@link LibFunction} which implements the features of the luajava package.
 * <p>
 * Luajava is an approach to mixing lua and java using simple functions that bind
 * java classes and methods to lua dynamically.  The API is documented on the
 * <a href="http://www.keplerproject.org/luajava/">luajava</a> documentation pages.
 * 
 * <p>
 * Typically, this library is included as part of a call to
 * {@link org.luaj.vm2.lib.jse.JsePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("luajava").get("bindClass").call( LuaValue.valueOf("java.lang.System") ).invokeMethod("currentTimeMillis") );
 * } </pre>
 * <p>
 * To instantiate and use it directly,
 * link it into your globals table via {@link Globals#load} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new LuajavaLib());
 * globals.load(
 *      "sys = luajava.bindClass('java.lang.System')\n"+
 *      "print ( sys:currentTimeMillis() )\n", "main.lua" ).call();
 * } </pre>
 * <p>
 * 
 * The {@code luajava} library is available
 * on all JSE platforms via the call to {@link org.luaj.vm2.lib.jse.JsePlatform#standardGlobals()}
 * and the luajava api's are simply invoked from lua.
 * Because it makes extensive use of Java's reflection API, it is not available
 * on JME, but can be used in Android applications.
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * 
 * @see LibFunction
 * @see org.luaj.vm2.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see LuaC
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 * @see <a href="http://www.keplerproject.org/luajava/manual.html#luareference">http://www.keplerproject.org/luajava/manual.html#luareference</a>
 */
public class LuajavaLib extends VarArgFunction {

	static final int INIT           = 0;
	static final int BINDCLASS      = 1;
	static final int NEWINSTANCE	= 2;
	static final int NEW			= 3;

	static final String[] NAMES = {
		"bindClass",
		"newInstance",
		"new",
	};
	
	static final int METHOD_MODIFIERS_VARARGS = 0x80;

	public LuajavaLib() {
	}

	public Varargs invoke(Varargs args) {
			try {
				switch ( opcode ) {
				case INIT: {
					// LuaValue modname = args.arg1();
					LuaValue env = args.arg(2);
					LuaTable t = new LuaTable();
					bind( t, this.getClass(), NAMES, BINDCLASS );
					env.set("luajava", t);
					if (!env.get("package").isnil()) env.get("package").get("loaded").set("luajava", t);
					return t;
				}
				case BINDCLASS: {
					final Class clazz = classForName(args.checkjstring(1));
					return JavaClass.forClass(clazz);
				}
				case NEWINSTANCE:
				case NEW: {
					// get constructor
					final LuaValue c = args.checkvalue(1);
					final Class clazz = (opcode==NEWINSTANCE? classForName(c.tojstring()): (Class) c.checkuserdata(Class.class));
					final Varargs consargs = args.subargs(2);
					return JavaClass.forClass(clazz).getConstructor().invoke(consargs);
				}
				default:
					throw new LuaError("not yet supported: "+this);
				}
			} catch (LuaError e) {
				throw e;
			} catch (Exception e) {
				throw new LuaError(e);
			}
		}

	// load classes using app loader to allow luaj to be used as an extension
	protected Class classForName(String name) throws ClassNotFoundException {
		return Class.forName(name, true, ClassLoader.getSystemClassLoader());
	}

}