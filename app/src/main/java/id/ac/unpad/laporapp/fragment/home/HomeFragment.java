package id.ac.unpad.laporapp.fragment.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import id.ac.unpad.laporapp.InputActivity;
import id.ac.unpad.laporapp.R;
import id.ac.unpad.laporapp.adapter.LaporAdapter;
import id.ac.unpad.laporapp.db.AppDatabase;
import id.ac.unpad.laporapp.model.Lapor;

public class HomeFragment extends Fragment {


    RecyclerView list;
    LaporAdapter adapter;
    FloatingActionButton fab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        list = root.findViewById(R.id.list);
        //layout di internal recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(layoutManager);
        //mendapatkan data dari sqlite (tabel lapor)
        List<Lapor> lapors = AppDatabase.getInstance(getActivity())
                .laporDao().getAll();
        adapter = new LaporAdapter(getActivity(),lapors);
        list.setAdapter(adapter);

        fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), InputActivity.class);
                startActivity(intent);
            }
        });
        return root;
    }
}