package pl.darenie.dna.configuration

import dagger.Component
import pl.darenie.dna.service.DareInstanceIDService
import pl.darenie.dna.ui.base.BaseActivity
import pl.darenie.dna.ui.accounting.AccountingFragment
import pl.darenie.dna.ui.bill.BillFragment
import pl.darenie.dna.ui.bill.BillListFragment
import pl.darenie.dna.ui.social.SocialFragment
import pl.darenie.dna.ui.social.FriendProfileFragment
import pl.darenie.dna.ui.social.SearchFriendFragment
import pl.darenie.dna.ui.main.MainActivity
import pl.darenie.dna.ui.login.LoginActivity
import pl.darenie.dna.ui.login.RegisterActivity
import pl.darenie.dna.ui.user.UserFragment
import javax.inject.Singleton



@Singleton
@Component(modules = arrayOf(
        AppModule::class,
        RestModule::class
))
interface RestComponent {
    fun inject(activity: LoginActivity)
    fun inject(registerActivity: RegisterActivity)
    fun inject(mainActivity: MainActivity)

    fun inject(searchFriendFragment: SearchFriendFragment)
    fun inject(billFragment: BillFragment)
    fun inject(socialFragment: SocialFragment)
    fun inject(accountingFragment: AccountingFragment)
    fun inject(friendProfileFragment: FriendProfileFragment)

    fun inject(dareInstanceIDService: DareInstanceIDService)
    fun inject(userFragment: UserFragment)
    fun inject(billListFragment: BillListFragment)
    fun inject(baseActivity: BaseActivity)
}