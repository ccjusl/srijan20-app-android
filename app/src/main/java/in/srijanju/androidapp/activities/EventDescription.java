package in.srijanju.androidapp.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.SrijanActivity;
import in.srijanju.androidapp.model.SrijanEvent;

public class EventDescription extends SrijanActivity {

  Button register;
  View.OnClickListener regClickListener;
  SrijanEvent event;
  FirebaseUser user;
  ValueEventListener registeredListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_event_description);

	Bundle extras = getIntent().getExtras();
	if (extras == null) {
	  Toast.makeText(this, "Something went wrong! :(", Toast.LENGTH_SHORT).show();
	  finish();
	  return;
	}
	event = (SrijanEvent) extras.getSerializable("event");
	if (event == null) {
	  Toast.makeText(this, "Something went wrong! :(", Toast.LENGTH_SHORT).show();
	  finish();
	  return;
	}

	ImageView poster = findViewById(R.id.poster);
	TextView event_name = findViewById(R.id.event_name);
	TextView event_type = findViewById(R.id.event_type);
	TextView event_desc = findViewById(R.id.event_description);
	TextView event_rules_heading = findViewById(R.id.event_rules);
	TextView event_rules = findViewById(R.id.event_rules_content);
	TextView event_contact = findViewById(R.id.event_contact_content);
	register = findViewById(R.id.button);

	Glide.with(this).load(event.poster).into(poster);
	event_name.setText(event.name);
	event_type.setText(event.type);
	event_desc.setText(event.desc);
	if (event.rules != null && !event.rules.equals("") && !event.rules.equals("none")) {
	  event_rules.setText(event.rules);
	  event_rules_heading.setVisibility(View.VISIBLE);
	  event_rules.setVisibility(View.VISIBLE);
	}
	event_contact.setText(event.poc);

	user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(getApplicationContext());
	  startActivity(new Intent(EventDescription.this, MainActivity.class));
	  finish();
	}

	regClickListener = new View.OnClickListener() {
	  public void onClick(View v) {
		if (event.maxts == 0) {
		  Toast.makeText(EventDescription.this, "Registration for this event not yet started", Toast.LENGTH_LONG).show();
		  return;
		}
		Intent myIntent = new Intent(EventDescription.this, EventRegister.class);
		// Put the event object of the event that was clicked
		Bundle bundle = new Bundle();
		bundle.putSerializable("event", event);
		myIntent.putExtras(bundle);
		startActivity(myIntent);
	  }
	};

	registeredListener = new ValueEventListener() {
	  @Override
	  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
		if (!dataSnapshot.exists()) {
		  register.setText(R.string.register);
		  register.setBackground(new ColorDrawable(getResources().getColor(android.R.color.black)));
		  register.setOnClickListener(regClickListener);
		  register.setClickable(true);
		} else {
		  register.setText(R.string.registered);
		  register.setBackground(new ColorDrawable(getResources().getColor(android.R.color.darker_gray)));
		  register.setOnClickListener(null);
		  register.setClickable(false);
		}
		register.setVisibility(View.VISIBLE);
	  }

	  @Override
	  public void onCancelled(@NonNull DatabaseError databaseError) {
	  }
	};
  }

  @Override
  protected void onResume() {
	super.onResume();
	FirebaseDatabase.getInstance().getReference("srijan/profile/" + user.getUid() + "/events/" + event.code).addValueEventListener(registeredListener);
  }

  @Override
  protected void onPause() {
	super.onPause();
	FirebaseDatabase.getInstance().getReference("srijan/profile/" + user.getUid() + "/events/" + event.code).removeEventListener(registeredListener);
  }
}
