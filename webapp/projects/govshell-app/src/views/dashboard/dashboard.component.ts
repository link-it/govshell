import { AfterContentChecked, Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { LangChangeEvent, TranslateService } from '@ngx-translate/core';

import { ConfigService } from 'projects/tools/src/lib/config.service';
import { EventsManagerService } from 'projects/tools/src/lib/eventsmanager.service';
import { EventType } from 'projects/tools/src/lib/classes/events';
import { PageloaderService } from 'projects/tools/src/lib/pageloader.service';
import { AuthenticationService } from '../../services/authentication.service';
import { Tools } from 'projects/tools/src/lib/tools.service';

import { INavData } from '../../containers/gp-layout/gp-sidebar-nav';
import { navItemsMainMenu } from '../../containers/gp-layout/_nav';
import { GpSidebarNavHelper } from '../../containers/gp-layout/gp-sidebar-nav.helper';
import { MenuAction } from 'projects/components/src/lib/classes/menu-action';

import urlExist from "url-exist"

@Component({
  selector: 'app-dashboard',
  templateUrl: 'dashboard.component.html',
  styleUrls: ['dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterContentChecked {
  static readonly Name = 'DashboardComponent';

  config: any;
  appConfig: any;

  _spin: boolean = false;
  desktop: boolean = false;

  gridCols = 3;

  navItems: INavData[] = [];

  breadcrumbs: any[] = [
    { label: 'APP.TITLE.Dashboard', url: '', type: 'title', icon: 'dashboard' }
  ];

  single: any[] = [];
  multi: any[] = [];
  sparklineData: any[] = [];

  chartOptions: any = null;

  view: any = null; // [700, 400];

  _menuAppActions: MenuAction[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private configService: ConfigService,
    public eventsManagerService: EventsManagerService,
    public pageloaderService: PageloaderService,
    public authenticationService: AuthenticationService,
    public sidebarNavHelper: GpSidebarNavHelper
  ) {
    this.appConfig = this.configService.getConfiguration();

    this.configService.getConfig('dashboard').subscribe(
      (config: any) => {
        this.config = config;
      }
    );

    // if (Tools.Applications.length > 0) {
    //   this._menuAppActions = Tools.Applications;
    // } else {
    //   this.configService.getConfig('application').subscribe(
    //     (config: any) => {
    //       const _apps = config.Applications || [];
    //       _apps.forEach(async (item: any) => {
    //         let _isEnabled = true;
    //         if (item.action !== 'dashboard') {
    //           _isEnabled = await urlExist(item.url);
    //         }
    //         this._menuAppActions.push(
    //           new MenuAction({ ...item, enabled: _isEnabled })
    //         );
    //       });
    //     }
    //   );
    // }

    this.navItems = [...navItemsMainMenu];
  }

  ngOnInit() {
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      // language changed
    });

    this.pageloaderService.resetLoader();
    this.pageloaderService.isLoading.subscribe({
      next: (x) => { this._spin = x; },
      error: (e: any) => { console.log('loader error', e); }
    });
  }

  ngOnDestroy() {
  }

  ngAfterContentChecked(): void {
    this.desktop = (window.innerWidth >= 992);
    this._menuAppActions = Tools.Applications;
  }

  _hasPermission(menu: any) {
    return this.authenticationService.hasPermission(menu.permission, 'view');
  }

  _dummyAction(event: any, param: any) {
    console.log(event, param);
  }

  onSelect(event: any) {
    // console.log(event);
  }

  _onMenuAppAction(menu: any) {
    if (menu.enabled) {
      Tools.CurrentApplication = { menu: menu };
      switch (menu.action) {
        case 'dashboard':
          this.router.navigate([menu.url]);
          break
        default:
          this.router.navigate(['/application']);
          break;
      }
    }
  }
}