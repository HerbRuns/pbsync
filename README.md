# PBSync

PBSync has been designed and developed to help players/clans sync their collection of 'personal best' times for bosses stored within Runelite.

The goal was to make all times available within Discord for use with bots/scripts to help create leaderboards, support events and give clan Discords the opportunity to show off new PBs as they are achieved.

Current features:
- Discord webhook integration. This was designed around having 2 channels:
  - a 'sync' channel
  - a 'new pb' channel
- A chat command used to sync all PBs current achieved:
  - !pbsync
- Send all newly achieved PBs to Discord as they are achieved

Note:
This plugin was originally designed for the use with the Mirage CC, but has been written in a manner that is (hopefully) general and global enough for all CCs to implement and build their own Discord channels + bots around the functionality.