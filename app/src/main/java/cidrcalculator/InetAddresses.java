
package cidrcalculator;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;

public class InetAddresses {

	  /**
	   * Evaluates whether the argument is an "IPv4 mapped" IPv6 address.
	   *
	   * <p>An "IPv4 mapped" address is anything in the range ::ffff:0:0/96
	   * (sometimes written as ::ffff:0.0.0.0/96), with the last 32 bits
	   * interpreted as an IPv4 address.
	   *
	   * <p>For more on IPv4 mapped addresses see section 2.5.5.2 of
	   * <a target="_parent"
	   *    href="http://tools.ietf.org/html/rfc4291#section-2.5.5.2"
	   *    >http://tools.ietf.org/html/rfc4291</a>
	   *
	   * <p>Note: This method takes a {@code String} argument because
	   * {@link InetAddress} automatically collapses mapped addresses to IPv4.
	   * (It is actually possible to avoid this using one of the obscure
	   * {@link Inet6Address} methods, but it would be unwise to depend on such
	   * a poorly-documented feature.)
	   *
	   * @param ipString {@code String} to be examined for embedded IPv4-mapped
	   *     IPv6 address format
	   * @return {@code true} if the argument is a valid "mapped" address
	   * @since 10.0
	   */
	  public static boolean isMappedIPv4Address(String ipString) {
	    byte[] bytes = ipStringToBytes(ipString);
	    if (bytes != null && bytes.length == 16) {
	      for (int i = 0; i < 10; i++) {
	        if (bytes[i] != 0) {
	          return false;
	        }
	      }
	      for (int i = 10; i < 12; i++) {
	        if (bytes[i] != (byte) 0xff) {
	          return false;
	        }
	      }
	      return true;
	    }
	    return false;
	  }

	  public static byte[] ipStringToBytes(String ipString) {
		    // Make a first pass to categorize the characters in this string.
		    boolean hasColon = false;
		    boolean hasDot = false;
		    for (int i = 0; i < ipString.length(); i++) {
		      char c = ipString.charAt(i);
		      if (c == '.') {
		        hasDot = true;
		      } else if (c == ':') {
		        if (hasDot) {
		          return null;  // Colons must not appear after dots.
		        }
		        hasColon = true;
		      } else if (Character.digit(c, 16) == -1) {
		        return null;  // Everything else must be a decimal or hex digit.
		      }
		    }

		    // Now decide which address family to parse.
		    if (hasColon) {
		      if (hasDot) {
		        ipString = convertDottedQuadToHex(ipString);
		        if (ipString == null) {
		          return null;
		        }
		      }

		    } else if (hasDot) {
		      return textToNumericFormatV4(ipString);
		    }
		    return null;
		  }

	  private static final int IPV4_PART_COUNT = 4;
		  private static byte[] textToNumericFormatV4(String ipString) {
		    String[] address = ipString.split("\\.", IPV4_PART_COUNT + 1);
		    if (address.length != IPV4_PART_COUNT) {
		      return null;
		    }

		    byte[] bytes = new byte[IPV4_PART_COUNT];
		    try {
		      for (int i = 0; i < bytes.length; i++) {
		        bytes[i] = parseOctet(address[i]);
		      }
		    } catch (NumberFormatException ex) {
		      return null;
		    }

		    return bytes;
		  }


	  private static String convertDottedQuadToHex(String ipString) {
		    int lastColon = ipString.lastIndexOf(':');
		    String initialPart = ipString.substring(0, lastColon + 1);
		    String dottedQuad = ipString.substring(lastColon + 1);
		    byte[] quad = textToNumericFormatV4(dottedQuad);
		    if (quad == null) {
		      return null;
		    }
		    String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
		    String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));
		    return initialPart + penultimate + ":" + ultimate;
		  }
	  
	  private static byte parseOctet(String ipPart) {
		    // Note: we already verified that this string contains only hex digits.
		    int octet = Integer.parseInt(ipPart);
		    // Disallow leading zeroes, because no clear standard exists on
		    // whether these should be interpreted as decimal or octal.
		    if (octet > 255 || (ipPart.startsWith("0") && ipPart.length() > 1)) {
		      throw new NumberFormatException();
		    }
		    return (byte) octet;
		  }
	  
	  private static short parseHextet(String ipPart) {
		    // Note: we already verified that this string contains only hex digits.
		    int hextet = Integer.parseInt(ipPart, 16);
		    if (hextet > 0xffff) {
		      throw new NumberFormatException();
		    }
		    return (short) hextet;
		  }

	  public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
		    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
		  }
	  /**
	   * Identify and mark the longest run of zeroes in an IPv6 address.
	   *
	   * <p>Only runs of two or more hextets are considered.  In case of a tie, the
	   * leftmost run wins.  If a qualifying run is found, its hextets are replaced
	   * by the sentinel value -1.
	   *
	   * @param hextets {@code int[]} mutable array of eight 16-bit hextets.
	   */
	  private static void compressLongestRunOfZeroes(int[] hextets) {
	    int bestRunStart = -1;
	    int bestRunLength = -1;
	    int runStart = -1;
	    for (int i = 0; i < hextets.length + 1; i++) {
	      if (i < hextets.length && hextets[i] == 0) {
	        if (runStart < 0) {
	          runStart = i;
	        }
	      } else if (runStart >= 0) {
	        int runLength = i - runStart;
	        if (runLength > bestRunLength) {
	          bestRunStart = runStart;
	          bestRunLength = runLength;
	        }
	        runStart = -1;
	      }
	    }
	    if (bestRunLength >= 2) {
	      Arrays.fill(hextets, bestRunStart, bestRunStart + bestRunLength, -1);
	    }
	  }

	  /** 
	   * Convert a list of hextets into a human-readable IPv6 address.
	   *
	   * <p>In order for "::" compression to work, the input should contain negative
	   * sentinel values in place of the elided zeroes.
	   *
	   * @param hextets {@code int[]} array of eight 16-bit hextets, or -1s.
	   */
	  private static String hextetsToIPv6String(int[] hextets) {
	    /*
	     * While scanning the array, handle these state transitions:
	     *   start->num => "num"     start->gap => "::"
	     *   num->num   => ":num"    num->gap   => "::"
	     *   gap->num   => "num"     gap->gap   => ""
	     */
	    StringBuilder buf = new StringBuilder(39);
	    boolean lastWasNumber = false;
	    for (int i = 0; i < hextets.length; i++) {
	      boolean thisIsNumber = hextets[i] >= 0;
	      if (thisIsNumber) {
	        if (lastWasNumber) {
	          buf.append(':');
	        }
	        buf.append(Integer.toHexString(hextets[i]));
	      } else {
	        if (i == 0 || lastWasNumber) {
	          buf.append("::");
	        }
	      }
	      lastWasNumber = thisIsNumber;
	    }
	    return buf.toString();
	  }

}
