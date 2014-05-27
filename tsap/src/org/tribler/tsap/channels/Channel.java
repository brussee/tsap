package org.tribler.tsap.channels;

import java.util.Map;
import java.util.Random;

/**
 * Class containing the information of a Dispersy channel
 * 
 * @author Dirk Schut
 */
public class Channel {
	private String name;
	private boolean following;
	private int torrentAmount;
	private int rating;
	private String dispersyCid;

	/**
	 * Constructor: initialize the instance variables to the values of the
	 * parameters
	 * 
	 * @param name
	 *            The name of the channel
	 * @param following
	 *            Whether the user is following this channel or not
	 * @param torrentAmount
	 *            The number of torrents in this channel
	 * @param rating
	 *            The rating of this channel
	 * @param dispersyCid
	 *            Id internally used by Dispersy
	 */
	public Channel(String name, boolean following, int torrentAmount,
			int rating, String dispersyCid) {
		this.name = name;
		this.following = following;
		this.torrentAmount = torrentAmount;
		this.rating = rating;
		this.dispersyCid = dispersyCid;
	}

	/**
	 * Constructor: creates a channel with name=name and the other instance
	 * variable are initiated with random values
	 * 
	 * @param name
	 *            The name of the channel
	 */
	public Channel(String name) {
		Random rand = new Random();
		this.name = name;
		this.following = rand.nextBoolean();
		this.torrentAmount = rand.nextInt(10000);
		this.rating = rand.nextInt(6);
		this.dispersyCid = "" + rand.nextInt() + rand.nextInt()
				+ rand.nextInt() + rand.nextInt() + rand.nextInt();
	}

	/**
	 * Constructor: creates a channel with data from dictionary from XMLRPC
	 * 
	 * @param XMLRPCdictionary
	 *            The dictionary returned from aXMLRPC
	 */
	public Channel(Map<String, Object> XMLRPCdictionary) {
		// TODO: load actual data
		this.name = (String) XMLRPCdictionary.get("name");
		this.following = false;
		this.torrentAmount = (Integer) XMLRPCdictionary.get("nr_torrent");
		this.rating = 0;
		this.dispersyCid = (String) XMLRPCdictionary.get("dispersy_cid");
	}

	/**
	 * Returns the name of the channel
	 * 
	 * @return The name of the channel
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return whether the user is following the channel
	 * 
	 * @return true iff the user is following this channel
	 */
	public boolean getFollowing() {
		return following;
	}

	/**
	 * Returns the number of torrents in this channel
	 * 
	 * @return The number of torrents in this channel
	 */
	public int getTorrentAmount() {
		return torrentAmount;
	}

	/**
	 * Returns the rating of this channel
	 * 
	 * @return The rating of this channel (0-5)
	 */
	public int getRating() {
		return rating;
	}

	/**
	 * Returns the string repesentation of the channel
	 * 
	 * @return The name of this channel
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns if two channels are equal by comparing their dispersyCid
	 * 
	 * @param other
	 *            Another Object. If this object is not another channel, false
	 *            is always returned.
	 */
	public boolean equals(Object other) {
		if (other instanceof Channel)
			return dispersyCid.equals(((Channel) other).dispersyCid);
		else
			return false;
	}

	/**
	 * Returns the DispersyCid
	 */
	public String getDispersyCid() {
		return dispersyCid;
	}
}