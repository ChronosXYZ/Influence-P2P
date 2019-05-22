package io.github.chronosx88.influence.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import io.github.chronosx88.influence.R
import io.github.chronosx88.influence.contracts.CoreContracts
import io.github.chronosx88.influence.models.GenericDialog
import io.github.chronosx88.influence.presenters.DialogListPresenter


class DialogListFragment : Fragment(), CoreContracts.IChatListViewContract {
    private lateinit var presenter: CoreContracts.IDialogListPresenterContract
    private lateinit var dialogList: DialogsList

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chatlist_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogList = view.findViewById(R.id.dialogsList)
        presenter = DialogListPresenter(this)
    }

    override fun setDialogAdapter(adapter: DialogsListAdapter<GenericDialog>) {
        dialogList.setAdapter(adapter)
    }

    override fun getActivityContext(): Context? {
        return context
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        presenter.onStop()
        super.onStop()
    }
}
