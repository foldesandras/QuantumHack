/**
 * Copyright (c) 2010 Daniel Murphy
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.quantumhackclient.analytics.dmurph;

/**
 * simple uri encoder, made from the spec at:
 * http://www.ietf.org/rfc/rfc2396.txt
 *
 * @author Daniel Murphy
 */
public class URIEncoder
{
	
	private static String mark = "-_.!~*'()\"";
	
	public static String encodeURI(String argString)
	{
		StringBuffer uri = new StringBuffer(); // Encoded URL
		
		char[] chars = argString.toCharArray();
		for(char c : chars)
			if(c >= '0' && c <= '9' || c >= 'a' && c <= 'z'
				|| c >= 'A' && c <= 'Z' || mark.indexOf(c) != -1)
				uri.append(c);
			else
			{
				uri.append("%");
				uri.append(Integer.toHexString(c));
			}
		return uri.toString();
	}
}
