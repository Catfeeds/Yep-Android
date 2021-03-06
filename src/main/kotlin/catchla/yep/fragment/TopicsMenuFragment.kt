package catchla.yep.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import catchla.yep.R
import catchla.yep.constant.topicsSortOrderKey
import catchla.yep.model.TopicSortOrder
import kotlinx.android.synthetic.main.fragment_spinner_floating_menu.*

/**
 * Created by mariotaku on 15/12/6.
 */
class TopicsMenuFragment : FloatingActionMenuFragment(), AdapterView.OnItemSelectedListener {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = ArrayAdapter<Entry>(context, android.R.layout.simple_list_item_1)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.add(Entry(TopicSortOrder.DISTANCE, getString(R.string.sort_order_nearby)))
        adapter.add(Entry(TopicSortOrder.TIME, getString(R.string.time)))
        adapter.add(Entry(TopicSortOrder.DEFAULT, getString(R.string.sort_order_match)))
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        val sortOrder = preferences[topicsSortOrderKey]
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).sortBy == sortOrder) {
                spinner.setSelection(i)
                break
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_spinner_floating_menu, container, false)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val fragment = belongsTo as TopicsListFragment?
        fragment?.reloadWithSortOrder((spinner.getItemAtPosition(position) as Entry).sortBy)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    internal class Entry(internal val sortBy: String, internal val title: String) {

        override fun toString(): String {
            return title
        }
    }
}
