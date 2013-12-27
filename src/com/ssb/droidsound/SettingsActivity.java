package com.ssb.droidsound;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.ssb.droidsound.database.HttpSongSource;
import com.ssb.droidsound.database.SongDatabase;
import com.ssb.droidsound.file.FileCache;

import com.ssb.droidsound.plugins.DroidSoundPlugin;

import com.ssb.droidsound.plugins.SidplayfpPlugin;
import com.ssb.droidsound.plugins.VICEPlugin;
import com.ssb.droidsound.utils.Log;

@SuppressWarnings("deprecation") // Old preference system
public class SettingsActivity extends PreferenceActivity {

	protected static final String TAG = SettingsActivity.class.getSimpleName();
	private SongDatabase songDatabase;
	private boolean doFullScan;
	public static SharedPreferences prefs;
	private String modsDir;
	
	class AudiopPrefsListener implements OnPreferenceChangeListener {
		
		private DroidSoundPlugin plugin;

		AudiopPrefsListener(DroidSoundPlugin pi) {
			plugin = pi;
		}
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String k = preference.getKey();
			String k2 = k.substring(k.indexOf('.')+1);
			
			Log.d(TAG, "CHANGED " + k);
			if(k.equals("SidPlugin.force_options"))
			{
				boolean forced = (Boolean) newValue ? true : false;
				findPreference("SidPlugin.sid_model").setEnabled(forced);
				findPreference("SidPlugin.video_mode").setEnabled(forced);
			}
				
			if(k.equals("SidPlugin.sidengine"))
			{
				boolean isVice = ((String) newValue).startsWith("VICE");
				boolean isSidplay2fp = ((String) newValue).startsWith("Sidplay2fp");
				
				findPreference("SidPlugin.resampling_mode").setEnabled(isVice);
				findPreference("SidPlugin.buildermode").setEnabled(isSidplay2fp);

			}
			
			if(newValue instanceof String)
			{
				try 
				{
					int i = Integer.parseInt((String) newValue);
					newValue = Integer.valueOf(i);
				} 
				catch (NumberFormatException e) {
				}
			}
			
			plugin.setOption(k2, newValue);
			return true;
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DroidSoundPlugin.setContext(getApplicationContext());
		Preference p = null;
		
		addPreferencesFromResource(R.layout.preferences);
		
		songDatabase = PlayerActivity.songDatabase;

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		modsDir = prefs.getString("modsDir", null);
		
		String s = prefs.getString("SidPlugin.sidengine", null);
		
		Preference resampling_mode_pref = findPreference("SidPlugin.resampling_mode");
		Preference buildermode = findPreference("SidPlugin.buildermode");
				
		boolean forced = prefs.getBoolean("SidPlugin.force_options", false);
		findPreference("SidPlugin.sid_model").setEnabled(forced);
		findPreference("SidPlugin.video_mode").setEnabled(forced);

		if(s.startsWith("Sidplay2fp"))
		{
			resampling_mode_pref.setEnabled(false);
			buildermode.setEnabled(true);
		} 

		if(s.startsWith("VICE"))
		{
			resampling_mode_pref.setEnabled(true);
			buildermode.setEnabled(false);
		} 
		
		Preference pref = findPreference("flush_cache");
				
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Log.d(TAG, "Flush directory listing");
				HttpSongSource.resetdirMap(getApplicationContext());
				return true;
			}
		});

		pref = findPreference("FileCache.fcsize");
				
		pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{		
			@Override
	
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Log.d(TAG, "setting fileCache size");
				int newval = Integer.parseInt((String) newValue);
				
				FileCache.getInstance().setLimitSize(newval * 1024 * 1024);
				Toast.makeText(getApplicationContext(), String.format("FileCache set to %d MB", newval), Toast.LENGTH_LONG).show();
								
				return true;
			}
		});
		
		
		pref = findPreference("rescan_pref");
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Log.d(TAG, "Rescan database");
				showDialog(R.string.scan_db);
				return true;
			}
		});
		
		pref = findPreference("help_pref");
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(SettingsActivity.this, HelpActivity.class));
				return true;
			}
		});

		pref = findPreference("download_link");
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				PackageInfo pinfo = null;
				try {
					pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				} catch (NameNotFoundException e) {}
				Intent intent;
				if(pinfo != null && pinfo.versionName.contains("beta")) {
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://swimsuitboys.com/droidsound/dl/beta.html"));
				} else {
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://swimsuitboys.com/droidsound/dl/"));
				}
				startActivity(intent);
				return true;
			}
		});	

		
		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {}

		List<DroidSoundPlugin> list = DroidSoundPlugin.createPluginList();
		String appName = getString(pinfo.applicationInfo.labelRes);
			
		PreferenceScreen aScreen = (PreferenceScreen) findPreference("audio_prefs");
		
		for(int i=0; i<aScreen.getPreferenceCount(); i++) {
			p = aScreen.getPreference(i);
			Log.d(TAG, "Pref '%s'", p.getKey());
			if(p instanceof PreferenceGroup) {
				PreferenceGroup pg = (PreferenceGroup) p;
				
				DroidSoundPlugin plugin = null;
				for(DroidSoundPlugin pl : list) {
					if(pl.getClass().getSimpleName().equals(pg.getKey())) {
						plugin = pl;
						break;
					}
				}
				if(plugin != null) {
					
					//String plname = plugin.getClass().getSimpleName();
					
					for(int j=0; j<pg.getPreferenceCount(); j++) {
						p = pg.getPreference(j);
						
						//p.setKey(plname + "." + p.getKey());
						
						Log.d(TAG, "Pref %s for %s", p.getKey(), plugin.getClass().getName());
						
						p.setOnPreferenceChangeListener(new AudiopPrefsListener(plugin));
					}
				}
			}
		}

		list.add(new SidplayfpPlugin());
		list.add(new VICEPlugin());
				
		PreferenceScreen abScreen = (PreferenceScreen) findPreference("about_prefs");

		if(abScreen != null) {
			PreferenceCategory pc = new PreferenceCategory(this);
			pc.setTitle("Droidsound");
			abScreen.addPreference(pc);
	
			p = new Preference(this);
			p.setTitle("Application");
			p.setSummary(String.format("%s v%s\n(C) 2010-2012 by Jonas Minnberg (Sasq)\n(c) 2013-201x by DroidMJT", appName, pinfo.versionName));		
			abScreen.addPreference(p);
	
			pc = new PreferenceCategory(this);
			pc.setTitle("Plugins");
			abScreen.addPreference(pc);
	
			for(DroidSoundPlugin pl : list) {
				String info = pl.getVersion();
				if(info != null) {
					p = new Preference(this);
					p.setTitle(pl.getClass().getSimpleName());
					p.setSummary(info);		
					abScreen.addPreference(p);
				}
			}
			
			pc = new PreferenceCategory(this);
			pc.setTitle("Other");
			abScreen.addPreference(pc);

			p = new Preference(this);
			p.setTitle("Icons");
			p.setSummary("G-Flat SVG by poptones");		
			abScreen.addPreference(p);

			p = new Preference(this);
			p.setTitle("ViewPageIndicator");
			p.setSummary("by Jake Wharton");		
			abScreen.addPreference(p);
		}

	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		switch(id) {		
		case R.string.recreate_confirm:
			builder.setMessage(id);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					songDatabase.rescan(modsDir);
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}				
			});
			break;
		case R.string.scan_db:
			doFullScan = false;
			builder.setTitle(id);
			builder.setMultiChoiceItems(R.array.scan_opts, null, new OnMultiChoiceClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					Log.d(TAG, "%d %s", which, String.valueOf(isChecked));
					doFullScan = isChecked;
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}				
			});
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					if(doFullScan) {
						showDialog(R.string.recreate_confirm);
					} else {
						songDatabase.scan(true, modsDir);
						finish();
					}
				}
			});
			break;

		default:
			builder.setMessage(id);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			break;
		}

		AlertDialog alert = builder.create();
		return alert;
	}

	
}
